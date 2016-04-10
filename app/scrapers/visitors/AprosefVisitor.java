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

package scrapers.visitors;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import play.Logger;
import scrapers.data.ScrapedIngredient;
import scrapers.data.ScrapedRecipe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Site visitor for recipe page aprosef.hu.
 *
 * @author Oliver Dozsa
 */
public class AprosefVisitor extends ListBasedVisitor
{
    /* --------------------------------------------------------------------- */
    /* ATTRIBUTES                                                            */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC ATTRIBUTES ------------------------------------------------ */
    
    /**
     * The unique id.
     */
    public static final int VISITOR_ID = 2;



    /* -- PROTECTED ATTRIBUTES --------------------------------------------- */



    /* -- PRIVATE ATTRIBUTES ----------------------------------------------- */

    /**
     * The site's url from which the recipes can be obtained.
     * */
    private static String BASE_URL = "http://aprosef.hu/receptek?combine=All&sort_by=nid&page=%d";

    /**
     * The main content selector. Selects the element containing the recipes.
     * */
    private static String RECIPE_MAIN_CONTENT_SELECTOR = "#leftside .view-content";
    
    /**
     * Default timeout for connecting to a recipe page.
     */
    private static final int RECIPE_CONNECT_TIMEOUT = 10000;

    /**
     * The connection.
     * */
    private Connection connection;

    /**
     * The main page.
     * */
    private Document mainPage;

    /**
     * Contains the recipe links. If it's empty, there are no more recipes to get.
     * */
    private Elements mainContent;
    


    /* --------------------------------------------------------------------- */
    /* METHODS                                                               */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC METHODS --------------------------------------------------- */

    /**
     * Initializes the visitor, and connects to the page.
     * */
    public AprosefVisitor()
    {
        super(VISITOR_ID);

        Logger.debug(AprosefVisitor.class.getName() + ".AprosefVisitor()");
    }

    @Override
    public boolean isConnected()
    {
        return mainPage != null;
    }

    @Override
    public void recover()
    {
        /* As all states are preserved in case of error, we can just go work state and try to continue processing. */
        changeState(VisitorState.WORK);
        
        errorCode = ErrorCode.ERROR_NO_ERROR;
    }
    
    /**
     * Scrapes the recipe from the given url.
     * 
     * @url    The url to scrape from.
     * 
     * @return The scraped recipe, or null if scraping failed.
     * @throws IOException 
     */
    public static ScrapedRecipe scrapeRecipeFromUrl(String url) throws IOException
    {
        ScrapedRecipe result = null;
        
        /* Try to get the recipe's page. */
        Connection recipeConnection = Jsoup.connect(url).timeout(RECIPE_CONNECT_TIMEOUT);
        
        Document recipePage = recipeConnection.get();

        /* Get recipe parts. */
        String name                         = scrapeRecipeName(recipePage);
        Integer servings                    = scrapeServings(recipePage);
        List<ScrapedIngredient> ingredients = scrapeIngredients(recipePage);

        /* Create recipe only, if everything went well. */
        if
        (
            name        != null &&
            servings    != null &&
            ingredients != null
        )
        {
            result = new ScrapedRecipe(url);

            result.setName(name);
            result.setServings(servings);
            result.setIngredientList(ingredients);
        }
        
        return result;
    }

    /* -- PROTECTED METHODS ------------------------------------------------ */

    /**
     * Moves to the next page.
     * */
    @Override
    protected void goToNextPage() throws IOException
    {
        /*
         * Page structure as of 19.05.2015.:
         * ...
         * <div id="leftside" class="clearfix">
         *     ...
         *     <div class = "view-content">
         *         ...
         *         <div class="views-row views-row-[N] views-row-odd views-row-first">
         *         </div>
         *         ...
         *     </div>
         *     ...
         * </div>
         * ...
         */

        Logger.debug(AprosefVisitor.class.getName() + ".goToNextPage():\n" +
            "    pageCounter = " + pageCounter);

        /* Get the first page of recipes. */
        connection = Jsoup.connect(String.format(BASE_URL, pageCounter));
        mainPage   = connection.get();

        /* Get the recipes of the first page. */
        mainContent    = mainPage.select(RECIPE_MAIN_CONTENT_SELECTOR);
        recipeElements = mainContent.select(".views-row");

        /* Increase page counter to point to next page to load. */
        pageCounter++;
    }

    /**
     * Scrapes a recipe from the recipe element.
     *
     * @param element    The element containing the recipe data (name, URL to detailed page).
     *
     * @return    The scraped recipe, or null, if scraping failed.
     */
    protected ScrapedRecipe scrapeFromElement(Element element)
    {
        /*
         * Element structure as of 19.05.2015.:
         *
         * <div class="views-row views-row-[N] views-row-odd views-row-first">
         *     ...
         *     <div class="views-field views-field-title">
         *         <span class="field-content">
         *             <a href="/[recipe_url]">
         *                 [Recipe name]
         *             </a>
         *         </span>
         *     </div>
         *     ...
         * </div>
         */

        ScrapedRecipe result = null;

        /* Extract the recipe's url. */
        Elements aElements = element.select(".views-field-title a");

        /* We expect exactly one <a> element. */
        if(aElements.size() == 1)
        {
            Element a = aElements.get(0);

            String absRecipeUrl = a.attr("abs:href");
            
            Logger.info(AprosefVisitor.class.getName() + ".scrapeFromElement(): Scraping recipe.\n" +
                "   recipeUrl  = " + absRecipeUrl);

            try
            {
                result = scrapeRecipeFromUrl(absRecipeUrl);
            }
            catch(IOException e)
            {
                /* Just print stack trace. Error handling is done in caller method. */
                e.printStackTrace();
            }
        }
        else
        {
            Logger.error(AprosefVisitor.class.getName() + ".scrapeFromElement(): Multiple <a> elements!\n" +
                "    aElements = " + aElements
            );
        }

        return result;
    }
    
    
    
    /* -- PRIVATE METHODS -------------------------------------------------- */

    /**
     * Scrapes the recipe name.
     *
     * @param document    The web page.
     *
     * @return The recipe name. Null is returned if couldn't get the name.
     */
    private static String scrapeRecipeName(Document document)
    {
        String result = null;

        Elements elements = document.select(".recipetitle");

        /* We expect exactly 1 element. */
        if(elements.size() == 1)
        {
            result = elements.get(0).text();
        }
        else
        {
            Logger.error(AprosefVisitor.class.getName() + ".scrapeRecipeName(): Multiple \".recipetitle\" elements!\n" +
                "    elements = " + elements
            );
        }

        return result;
    }

    /**
     *
     * Scrapes the ingredients.
     *
     * @param document    The web page.
     *
     * @return The scraped ingredients, or null if couldn't get them.
     */
    private static List<ScrapedIngredient> scrapeIngredients(Document document)
    {
        /*
         * Element structure as of 20.05.2015.:
         *
         * <div class="field_counted_ingredients">
         *     ...
         *     <ul>[Ingredients list]
         *         ...
         *         <li>[Ingredient data]</li>
         *         ...
         *         <li>[Ingredient data]</li>
         *         ...
         *     </ul>
         *     ...
         *     <ul>[Ingredients list]
         *         ...
         *         <li>[Ingredient data]</li>
         *         ...
         *         <li>[Ingredient data]</li>
         *         ...
         *     </ul>
         *     ...
         * </div>
         */

        List<ScrapedIngredient> result = new ArrayList<ScrapedIngredient>();

        Elements ingredientListElements = document.select(".field_counted_ingredients ul");

        /* Used for interrupting the scraping if an error is detected */
        boolean errorDetected = false;

        if(ingredientListElements.size() > 0)
        {
            /* There can be more than one ingredient lists. */
            for(Element ingredientList: ingredientListElements)
            {
                /* Check for errors. */
                if(errorDetected)
                {
                    break;
                }

                Elements ingredientElements = ingredientList.select("li");

                for(Element ingredientElement: ingredientElements)
                {
                    ScrapedIngredient ingredient = retrieveIngredient(ingredientElement);

                    if(ingredient != null)
                    {
                        result.add(ingredient);
                    }
                    else
                    {
                        errorDetected = true;

                        break;
                    }
                }
            }
        }
        else
        {
            Logger.error(AprosefVisitor.class.getName() + ".scrapeIngredients(): Couldn't find any ingredients!");
        }

        if(errorDetected)
        {
            /* In case of error, drop the result. */
            result = null;
        }

        return result;
    }

    /**
     * Scrapes the serving.
     *
     * @param document    The web page.
     *
     * @return The number of servings, or null, if couldn't get servings.
     */
    private static Integer scrapeServings(Document document)
    {
        Integer result = null;

        Elements servingElements = document.select(".counterbox");

        if(servingElements.size() == 1)
        {
            Element servingElement = servingElements.get(0);
            String servingText     = servingElement.text();

            if(servingText.contains("adag"))
            {
                /* Extract servings number. */
                Elements servingNumberElements = servingElement.select("#number");

                if(servingNumberElements.size() == 1)
                {
                    Element servingNumberElement = servingNumberElements.get(0);
                    String servingNumberStr      = servingNumberElement.attr("value");

                    try
                    {
                        result = Integer.parseInt(servingNumberStr);
                    }
                    catch(Exception e)
                    {
                        Logger.warn(AprosefVisitor.class.getName() + ".scrapeServings(): Couldn't parse serving value!\n" +
                            "    servingNumberStr = " + servingNumberStr
                        );
                    }
                }
            }
        }

        if(result == null)
        {
            Logger.info(AprosefVisitor.class.getName() + ".scrapeServings(): Assuming 2 servings.\n");

            /* We just assume, that the recipe is for 2 servings. */
            result = new Integer(2);
        }

        return result;
    }

    /**
     * Gets the ingredient from it's container element.
     *
     * @param element    The element containing ingredient data.
     *
     * @return The ingredients, or null, if failed to get it.
     */
    private static ScrapedIngredient retrieveIngredient(Element element)
    {
        ScrapedIngredient result = null;

        /* Get container elements. */
        Element amountElement = null;
        Element nameElement   = null;

        Elements amountElements = element.select("#mertek_");
        if(amountElements.size() == 1)
        {
            amountElement = amountElements.get(0);
        }
        
        nameElement = element;

        /* Retrieve data. */
        String combinedName = nameElement.text();
        String name         = null;
        String measure      = null;
        Double amount       = 0.0;
        
        if(combinedName != null)
        {
            if(amountElement != null)
            {
                String amountStr = amountElement.text();
                
                try
                {
                    amount = Double.parseDouble(amountStr);
                }
                catch(Exception e)
                {
                    Logger.error(AprosefVisitor.class.getName() + "retrieveIngredient(): Failed to parse amount!\n" +
                        "    amountStr = " + amountStr);
                }
                
                combinedName = combinedName.split(amountStr)[1];
                
                String[] nM = combinedName.split(" ");
                
                if(nM.length < 3)
                {
                    Logger.error(AprosefVisitor.class.getName() + "retrieveIngredient(): Name and measure can't be parsed!\n" +
                        "    combinedName = " + combinedName);
                }
                else
                {
                    name = "";
                    
                    for(int i = 2; i < nM.length - 1; i++)
                    {
                        name = name + nM[i] + " ";
                    }
                    
                    name = name + nM[nM.length - 1];
                    
                    measure = nM[1];
                }
            }
            else
            {
                Logger.info(AprosefVisitor.class.getName() + "retrieveIngredient(): amountElement is null!");
                
                /* Amount can be null. */
                name    = combinedName;
                amount  = 0.0;
                measure = "to taste";
            }
        }
        else
        {
            Logger.error(AprosefVisitor.class.getName() + "retrieveIngredient(): combinedName is null!");
        }
        
        if(name != null && amount != null && measure != null)
        {
            Logger.info(AprosefVisitor.class.getName() + "retrieveIngredient(): measure = " + measure);
            
            result = new ScrapedIngredient(name, measure, amount);
        }
        else
        {
            Logger.error(AprosefVisitor.class.getName() + "retrieveIngredient(): one of required data is null!\n" + 
                "     name    = " + name + "\n" +
                "     amount  = " + amount + "\n" +
                "     measure = " + measure + "\n"
            );
        }

        return result;
    }



    /* --------------------------------------------------------------------- */
    /* OTHERS                                                                */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC OTHERS ---------------------------------------------------- */



    /* -- PROTECTED OTHERS ------------------------------------------------- */



    /* -- PRIVATE OTHERS --------------------------------------------------- */
}
