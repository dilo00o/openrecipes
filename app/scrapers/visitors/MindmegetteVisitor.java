/*
 *  Copyright 2013 - 2015 Oliver Dozsa
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import play.Logger;
import scrapers.data.ScrapedRecipe;
import scrapers.data.ScrapedIngredient;

/**
 * Visitor for the recipe page http://www.mindmegette.hu.
 *
 * @author Oliver Dozsa
 */
public class MindmegetteVisitor extends ListBasedVisitor
{
    
    /* --------------------------------------------------------------------- */
    /* ATTRIBUTES                                                            */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC ATTRIBUTES ------------------------------------------------ */
    
    /**
     * The visitor's id.
     */
    public static final int VISITOR_ID = 3;



    /* -- PROTECTED ATTRIBUTES --------------------------------------------- */



    /* -- PRIVATE ATTRIBUTES ----------------------------------------------- */
    
    /**
     * The site's url from which the recipes can be obtained.
     * */
    private static final String BASE_URL = "http://www.mindmegette.hu/receptek-a-z-ig/osszes/";
    
    /**
     * Select the container of number of pages.
     */
    private static final String NUMBER_OF_PAGES_SELECTOR = ".current-page";    
    
    /**
     * There are no servings given in Mindmegette, so the fixed number of 4 is used.
     */
    private static final int SERVINGS = 4;
    
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
     * Stores the page links.
     */
    private List<String> pageLinks;
    
    /**
     * Points to the actual page-link to process.
     */
    private int pageLinkCounter;
    
    
    
    

    /* --------------------------------------------------------------------- */
    /* METHODS                                                               */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC METHODS --------------------------------------------------- */
    
    /**
     * Initializes the visitor, and connects to the page.
     * */
    public MindmegetteVisitor()
    {
        super(VISITOR_ID);
    }

    @Override
    public boolean isConnected()
    {
        boolean result = mainPage != null;
        
        return result;
    }

    @Override
    public void recover()
    {
        /* Just reset the working state. */
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
        
        Document recipeDocument = Jsoup.connect(url).timeout(RECIPE_CONNECT_TIMEOUT).get();
        
        List<ScrapedIngredient> ingredients = scrapeIngredients(recipeDocument);
        int servings                        = scrapeServings();
        
        if(ingredients != null)
        {
            result = new ScrapedRecipe(url);
            
            result.setName(""); // TODO: fix name
            result.setIngredientList(ingredients);
            result.setServings(servings);
        }
        else
        {
            Logger.error(MindmegetteVisitor.class.getName() + ".scrapeRecipeFromUrl(): ingredients is null!\n" +
                "    url = " + url);
        }
        
        return result;
    }



    /* -- PROTECTED METHODS ------------------------------------------------ */
    
    @Override
    protected ScrapedRecipe scrapeFromElement(Element element)
    {   
        ScrapedRecipe result = null;
        
        Element recipeA = element.select("a").first();
        
        String recipeName = recipeA.text().trim();
        String recipeUrl  = recipeA.attr("abs:href");
        
        Logger.info(MindmegetteVisitor.class.getName() + ".scrapeFromElement(): Scraping recipe.\n" +
            "   recipeUrl  = " + recipeUrl + "\n" +
            "   recipeName = " + recipeName);
        
        try
        {
            result = scrapeRecipeFromUrl(recipeUrl);
            
            if(result == null)
            {
                /* Log problem. */
                Logger.error(MindmegetteVisitor.class.getName() + ".scrapeFromElement(): Data for ScrapedRecipe is not OK!\n");
            }
            else
            {
                result.setName(recipeName);
            }
        }
        catch(IOException e)
        {
            Logger.error(MindmegetteVisitor.class.getName() + ".scrapeFromElement(): Failed to connect to recipe URL!\n" +
                "    recipeUrl = " + recipeUrl);
            
            e.printStackTrace();
        }
        
        return result;
    }

    @Override
    protected void goToNextPage() throws Exception
    {   
        Logger.debug(MindmegetteVisitor.class.getName() + ".goToNextPage():\n" +
            "    pageCounter = " + pageCounter);
        
        /* Some setup before first access. */
        if(pageLinks == null)
        {
            pageCounter = 1;
            
            pageLinks = new ArrayList<String>();
            
            pageLinkCounter = 0;
            
            getPageLinks();
        }
        
        /* Get the page of recipes. */
        String actualPageLink = pageLinks.get(pageLinkCounter) + "?p=%d";
        
        connection = Jsoup.connect(String.format(actualPageLink, pageCounter));
        mainPage   = connection.get();
        
        int numberOfPages = scrapeNumberOfPages();
        
        if(pageCounter <= numberOfPages)
        {
            recipeElements = mainPage.select(".recipe-item");
            
            /* Increase page counter to point to next page to load. */
            pageCounter++;
        }
        else
        {
            if(pageLinkCounter < pageLinks.size())
            {
                /* Go to next link. */
                pageLinkCounter++;
                
                /* Reset page counter. */
                pageCounter = 1;
            }
            else
            {
                /* No more pages to load. */
                recipeElements = new ArrayList<>();
            }
        }        
    }



    /* -- PRIVATE METHODS -------------------------------------------------- */
    
    /**
     * Scrapes the number of pages.
     * 
     * @return
     */
    private int scrapeNumberOfPages()
    {
        int result = 0;
        
        Element numOfPagesElement =  null;
        
        numOfPagesElement = mainPage
            .select(NUMBER_OF_PAGES_SELECTOR).first()
            .select("h4").first();
        
        String allPagesStr = numOfPagesElement.text().split("/")[1].trim();
        
        result = Integer.parseInt(allPagesStr);
        
        return result;
    }
    
    /**
     * Scrapes the ingredient from the recipe page.
     * 
     * @param document    The recipe page to scrape from.
     * 
     * @return List of scraped ingredients, or null if failed to get them.
     */
    private static List<ScrapedIngredient> scrapeIngredients(Document document)
    {
        List<ScrapedIngredient> result = null;
        
        Element ingredientsUl = document.select("#ingredient-lists").select(".list").select("ul").first();
        
        boolean oldFormat = false;
        
        if(ingredientsUl == null)
        {
            /* Could be the old format is used. */
            ingredientsUl = document.select("#ingredient-lists").select(".hozzavalok-lista").first();
            
            oldFormat = true;
            
            Logger.info(MindmegetteVisitor.class.getName() + ".scrapeIngredients(): Old format is used!");
        }
        
        Elements ingredientsLis = ingredientsUl.select("li");
        
        int i = 0;
        
        if(oldFormat)
        {
            i++;
        }
        
        for(; i < ingredientsLis.size(); i++)
        {
            Element ingredientLi = ingredientsLis.get(i);
            
            if(result == null)
            {
                /* Create result list if found at least 1 ingredient. */
                result = new ArrayList<ScrapedIngredient>();
            }
            
            ScrapedIngredient scrpdIngredient = null;
            
            Logger.info(MindmegetteVisitor.class.getName() + ".scrapeIngredients():\n" + 
                "   ingredientLi.text() = " + ingredientLi.text());
            
            Double amount  = 0.0;
            String name    = "";
            String measure = "";
            
            if(oldFormat)
            {
                String[] content = ingredientLi.text().trim().split(" ");
                
                /* Check if there are at least 3 parts. */
                if(content.length > 2)
                {
                    /* We assume the first two word contains the amount, and the measure, and the rest is the name. */
                    amount  = 0.0;
                    
                    try
                    {
                        amount = Double.parseDouble(content[0]);
                    }
                    catch(Exception e)
                    {   
                        Logger.warn(MindmegetteVisitor.class.getName() + ".scrapeIngredients(): Failed to parse amount!\n" +
                            "    amount text = " + content[0]);
                        
                        /* Assume 0. Correction might be needed in DB. */
                        amount = 0.0;
                    }
                    
                    measure = content[1];
                    name    = StringUtils.join(content, " ", 2, content.length);
                }
                else
                {
                    /* Assume 0 amount, and "to taste" measure. */
                    amount  = 0.0;
                    measure = "to taste";
                    name    = StringUtils.join(content, " ");
                }
            }
            else
            {
                String amountStr = ingredientLi.select(".ingredient-measure").select(".amount").first().text().trim();
                measure          = ingredientLi.select(".ingredient-measure").select(".unit").first().text().trim();
                name             = ingredientLi.select(".ingredient-name").text().trim();
                
                if(amountStr != null)
                {
                    try
                    {
                        amount = Double.parseDouble(amountStr);
                    }
                    catch(Exception e)
                    {   
                        Logger.warn(MindmegetteVisitor.class.getName() + ".scrapeIngredients(): Failed to parse amount!\n" +
                            "    amount text = " + amountStr);
                        
                        /* Assume 0. Correction might be needed in DB. */
                        amount = 0.0;
                    }
                }
                
                if(measure == null || measure.equalsIgnoreCase(""))
                {
                    measure = "to taste";
                }
            }
            
            scrpdIngredient = new ScrapedIngredient(name, measure, amount);
            
            result.add(scrpdIngredient);
        }
        
        return result;
    }
    
    /**
     * Scrapes the servings.
     * 
     * @return The servings.
     */
    private static int scrapeServings()
    {
        /* There are no servings given in Mindmegette, so a fixed number is returned. */
        int result = SERVINGS;
        
        return result;
    }
    
    /**
     * Gets the page links used for further scraping.
     */
    private void getPageLinks()
    {
        /* Getting valid page links. */
        Connection localConn = Jsoup.connect(BASE_URL);
        
        try
        {
            Document localDoc = localConn.get();
            
            Elements collectionElems = localDoc.select("#collection");
            
            if(collectionElems.size() == 1)
            {
                Elements ddElems = collectionElems.select("dd");
                
                for(Element ddElem: ddElems)
                {
                    Element aElem = ddElem.select("a").first();
                    
                    pageLinks.add(aElem.attr("abs:href"));
                }
            }
            else
            {
                Logger.error(MindmegetteVisitor.class.getName() + ".getPageLinks(): Failed to get collection.");
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }



    /* --------------------------------------------------------------------- */
    /* OTHERS                                                                */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC OTHERS ---------------------------------------------------- */



    /* -- PROTECTED OTHERS ------------------------------------------------- */



    /* -- PRIVATE OTHERS --------------------------------------------------- */
}
