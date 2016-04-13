 /*
 *  Copyright 2016 Oliver Dozsa
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package scrapers.dbload;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import controllers.privately.Home;
import models.*;
import play.Logger;
import scrapers.data.ScrapedIngredient;
import scrapers.data.ScrapedRecipe;
import scrapers.visitors.RecipeSiteVisitor;
import scrapers.visitors.RecipeSiteVisitor.ErrorCode;

/**
 * Loads the database with data from a given scraper.
 *
 * @author Oliver Dozsa
 */
public class DbLoader
{

    /* --------------------------------------------------------------------- */
    /* ATTRIBUTES                                                            */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC ATTRIBUTES ------------------------------------------------ */



    /* -- PROTECTED ATTRIBUTES --------------------------------------------- */



    /* -- PRIVATE ATTRIBUTES ----------------------------------------------- */

    /**
     * The visitor.
     */
    private RecipeSiteVisitor visitor;

    /**
     * The maximum number of trials for recovering.
     */
    private static final int MAX_NUM_OF_RETRIES = 5;
    
    /**
     * Used for ingredient matching.
     * */
    private static final double MATCH_SCORE_THRESHOLD = 0.88;
    
    /**
     * Shows whether the state of the loading is working.
     */
    private boolean isWorking;
    
    /**
     * Shows whether there was error detected not.
     */
    private boolean isError;
    
    /**
     * Recipe scraped listener.
     */
    private OnRecipeScraped recipeScrapedListener;
    
    /**
     * The name.
     */
    private String name;
    
    /**
     * Used for stopping the load.
     */
    private boolean stopLoad = false;
    
    /**
     * The language id used for scraping.
     */
    private Long languageId = 0L;
    
    



    /* --------------------------------------------------------------------- */
    /* METHODS                                                               */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC METHODS --------------------------------------------------- */

    /**
     * Initializes the loader with visitor and source page id.
     *
     * @param visitor     The visitor.
     * @param sourceId    The source ID.
     */
    public DbLoader(RecipeSiteVisitor visitor, Long languageID)
    {
        Logger.debug(DbLoader.class.getName() + ".DbLoader()["+ visitor.getClass().getSimpleName() +"]:\n" +
            "    visitor    = "  + visitor.getClass().getName() + "\n" +
            "    languageId = " + languageId
        );

        this.visitor    = visitor;
        this.languageId = languageID;
        
        isWorking = false;
        isError   = false;
        
        name = visitor.getClass().getSimpleName();
    }

    /**
     * Loads the database.
     */
    public void load()
    {
        isWorking = true;
        isError   = false;
        
        ScrapedRecipe recipe = visitor.nextElement();

        if(recipe == null)
        {
            /* Try to get it again. */
            recipe = retryScrape();

            if(recipe == null)
            {
                /* Failed to get first recipe. */
                Logger.warn(DbLoader.class.getName() + ".load()["+ visitor.getClass().getSimpleName() +"]: Failed to get first recipe.");
            }
        }

        while(recipe != null && !stopLoad)
        {
            addRecipeToDb(recipe);
            
            if(recipeScrapedListener != null)
            {
                recipeScrapedListener.onRecipeScraped(recipe);
            }

            recipe = visitor.nextElement();

            if(recipe == null)
            {
                /* Try to recover in case of error, otherwise there's nothing more to get. */
                if(visitor.getState() == RecipeSiteVisitor.VisitorState.ERROR)
                {
                    recipe = retryScrape();
                }
            }
        }
        
        /* Reset stopload status. */
        stopLoad = false;
        
        isWorking = false;
    }
    
    
    /**
     * Sets the on scraped recipe listener.
     * 
     * @param eventListener    The event listener.
     */
    public void setOnRecipeScraped(OnRecipeScraped eventListener)
    {
        recipeScrapedListener = eventListener;
    }
    
    /**
     * Used for stopping the database load.
     */
    public void stopLoad()
    {
        stopLoad = true;
    }
    
    /**
     * Checks whether working is in progress.
     * 
     * @return True if work is in progress.
     */
    public boolean isWorking()
    {
        return isWorking;
    }
    
    /**
     * Checks whether there's an error detected or not.
     * 
     * @return True if an error is detected.
     */
    public boolean isError()
    {
        return isError;
    }
    
    /**
     * Gets the name.
     * 
     * @return The name.
     */
    public String getName()
    {
        return name;
    }



    /* -- PROTECTED METHODS ------------------------------------------------ */



    /* -- PRIVATE METHODS -------------------------------------------------- */

    /**
     * Retries scraping, if the visitor is in error state.
     *
     * @return The scraped recipe, or null, if there are no more recipes to scrape,
     * or there's an error.
     */
    private ScrapedRecipe retryScrape()
    {
        Logger.debug(DbLoader.class.getName() + ".retryScrape()["+ visitor.getClass().getSimpleName() +"]");

        ScrapedRecipe result = null;

        for(int i = 0; i < MAX_NUM_OF_RETRIES; i++)
        {
            Logger.info(DbLoader.class.getName() + ".retryScrape()["+ visitor.getClass().getSimpleName() +"]:\n" +
                "    i = " + i
            );

            visitor.recover();

            result = visitor.nextElement();

            if(result != null)
            {
                /* Recover successful. */
                Logger.info(DbLoader.class.getName() + ".retryScrape()["+ visitor.getClass().getSimpleName() +"]: Recover successful!");

                break;
            }
            else if(i == (MAX_NUM_OF_RETRIES - 1))
            {
                if(visitor.getErrorCode() == ErrorCode.ERROR_SCRAPE)
                {
                    /* Failed recover, try skipping element. */
                    Logger.warn(DbLoader.class.getName() + ".retryScrape()["+ visitor.getClass().getSimpleName() +"]: Recover failed, try skipping element!");
                    
                    visitor.skipCurrentElement();
                    
                    i = 0;
                }
                else if(visitor.getErrorCode() == ErrorCode.ERROR_PAGE_LOAD)
                {
                    Logger.warn(DbLoader.class.getName() + ".retryScrape()["+ visitor.getClass().getSimpleName() +"]: Recover failed, try skipping page!");
                    
                    visitor.skipCurrentPage();
                    
                    i = 0;
                }
            }
        }
        
        if(result == null)
        {
            isError = true;
        }

        return result;
    }
    
    /**
     * Adds the given recipe to the DB. It assumes, that all measures, and ingredients are already added.
     *
     * @param recipe The recipe to add.
     */
    private void addRecipeToDb(ScrapedRecipe recipe)
    {
        /* Stores the mapping between scraped ingredients and DB ingredients. */
        Map<String, Ingredient> scrpIngToDbIng = new HashMap<String, Ingredient>();
        
        for(ScrapedIngredient scrpIng: recipe.getIngredientList())
        {
            if(scrpIng.getName() != null)
            {
                Ingredient bestMatchIng = findBestMatch(scrpIng, languageId);
                
                if(bestMatchIng != null)
                {
                    scrpIngToDbIng.put(scrpIng.getName(), bestMatchIng);
                }
                else
                {
                    /* Ignore recipe if there's an ingredient missing. */
                    scrpIngToDbIng.clear();
                    
                    break;
                }
            }
        }
        
        if(scrpIngToDbIng.values().size() > 0)
        {
            Recipe dbRecipe = new Recipe();
            dbRecipe.name   = recipe.getName();
            dbRecipe.url    = recipe.getUrl();
            
            dbRecipe.save();
            
            for(Ingredient dbIng: scrpIngToDbIng.values())
            {
                RecipeIngredient recIng = new RecipeIngredient();
                
                recIng.ingredient = dbIng;
                recIng.recipe     = dbRecipe;
                
                recIng.save();
            }
        }
        else
        {
            Logger.warn(Home.class.getName() + ".saveScrapedRecipeToDb(): No ingredients for scraped recipe!\n" +
                "    scrapedRecipe.name = " + recipe.getName());
        }
    }
    
    /**
     * Finds the best match for a scraped ingredient to a DB ingredient.
     * 
     * @param ingredient    The scraped ingredient.
     * @param language      The language id.
     * @return The DB ingredient. Null is returned if no appropriate ingredient found.
     */
    private Ingredient findBestMatch(ScrapedIngredient ingredient, Long language)
    {
        Ingredient result         = null;
        String resultName         = null;
        double maxJaroWinklerDist = 0.0;
        
        List<IngredientName> allNamesByLang = IngredientName.find
            .fetch("ingredient")
            .where()
                .eq("language.id", language)
            .findList();
        
        for(IngredientName dbIngredientName: allNamesByLang)
        {   
            String name = dbIngredientName.name;
            
            double jaroWinkDist = StringUtils.getJaroWinklerDistance(ingredient.getName(), name);
            
            if(jaroWinkDist > maxJaroWinklerDist)
            {
                /* Found a better match. */
                maxJaroWinklerDist = jaroWinkDist;
                result             = dbIngredientName.ingredient;
                resultName         = name;
            }
        }
        
        if((maxJaroWinklerDist < MATCH_SCORE_THRESHOLD))
        {
            /* If no good result was found try with */
            result = findBestMatchByAlias(ingredient.getName(), language);
        }
        
        Logger.debug(Home.class.getName() + ".findBestMatch(): result\n" +
            "   dbName      = " + resultName + "\n" + 
            "   scrapedName = " + ingredient.getName() + "\n"
            "   score       = " + maxJaroWinklerDist +  "\n" +
            "   accepted    = " + (maxJaroWinklerDist >= MATCH_SCORE_THRESHOLD)
        );
        
        return result;
    }
    
    /**
     * Tries to find the best ingredient match based on alias search.
     * 
     * @param searchName    The search name.
     * @param languageID    The language id.
     * @return The best match or null.
     */
    private Ingredient findBestMatchByAlias(String searchName, Long languageID)
    {
        Ingredient result         = null;
        String resultName         = null;
        double maxJaroWinklerDist = 0.0;
        
        List<IngredientAlias> allAliasNamesByLang = IngredientAlias.find
            .fetch("ingredient")
            .where()
                .eq("language.id", languageID)
            .findList();
        
        for(IngredientAlias dbIngredientAliasName: allAliasNamesByLang)
        {   
            String name = dbIngredientAliasName.name;
            
            double jaroWinkDist = StringUtils.getJaroWinklerDistance(searchName, name);
            
            if(jaroWinkDist > maxJaroWinklerDist)
            {
                /* Found a better match. */
                maxJaroWinklerDist = jaroWinkDist;
                result             = dbIngredientAliasName.ingredient;
                resultName         = name;
            }
        }
        
        if((maxJaroWinklerDist < MATCH_SCORE_THRESHOLD))
        {
            result = null;
        }
        
        Logger.debug(Home.class.getName() + ".findBestMatchByAlias(): result\n" +
            "   dbName      = " + resultName + "\n" + 
            "   searchName  = " + searchName +
            "   score       = " + maxJaroWinklerDist +  "\n" +
            "   accepted    = " + (maxJaroWinklerDist >= MATCH_SCORE_THRESHOLD)
        );
        
        return result;
    }
    
    



    /* --------------------------------------------------------------------- */
    /* OTHERS                                                                */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC OTHERS ---------------------------------------------------- */
    
    /**
     * Used for notification of scraped recipes.
     */
    public static interface OnRecipeScraped
    {
        public void onRecipeScraped(ScrapedRecipe scraped);
    }



    /* -- PROTECTED OTHERS ------------------------------------------------- */



    /* -- PRIVATE OTHERS --------------------------------------------------- */

}
