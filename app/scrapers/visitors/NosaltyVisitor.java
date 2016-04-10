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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import play.Logger;
import scrapers.data.ScrapedIngredient;
import scrapers.data.ScrapedRecipe;

/**
 * Visitor class for web page http://nosalty.hu.
 *
 * @author Oliver Dozsa
 */
public class NosaltyVisitor extends ListBasedVisitor
{
    /* --------------------------------------------------------------------- */
    /* ATTRIBUTES                                                            */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC ATTRIBUTES ------------------------------------------------ */
    
    /**
     * The visitor's id.
     */
    public static final int VISITOR_ID = 4;



    /* -- PROTECTED ATTRIBUTES --------------------------------------------- */



    /* -- PRIVATE ATTRIBUTES ----------------------------------------------- */
    
    /**
     * The site's url from which the recipes can be obtained.
     * */
    private static final String BASE_URL = "http://www.nosalty.hu/receptek/adag/osszes?page=0%%2C%d";
    
    /**
     * The main content selector. Selects the element containing the recipes.
     * */
    private static final String RECIPE_MAIN_CONTENT_SELECTOR = ".article-list-horizontal";
    
    /**
     * Default timeout for connecting to a recipe page.
     */
    private static final int RECIPE_CONNECT_TIMEOUT = 10000;
    
    /**
     * The connection.
     */
    private Connection connection;

    /**
     * The main page.
     */
    private Document mainPage;
    
    /**
     * Used to decide whether the last page is reached or not.
     * Nosalty gives the last valid page if the page above the max. is reached.
     */
    private String firstRecipeUrl;
    

    /* --------------------------------------------------------------------- */
    /* METHODS                                                               */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC METHODS --------------------------------------------------- */
    
    /**
     * The default constructor.
     */
    public NosaltyVisitor()
    {
        super(VISITOR_ID);
        
        firstRecipeUrl = "";
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
        
        String name = recipeDocument.select("h1").get(0).text().trim().replaceAll(" recept", "");
        
        List<ScrapedIngredient> ingredients = scrapeIngredients(recipeDocument);
        int servings                        = scrapeServings(recipeDocument);
        
        if(ingredients != null)
        {
            result = new ScrapedRecipe(url);
            
            result.setName(name);
            result.setIngredientList(ingredients);
            result.setServings(servings);
        }
        
        return result;
    }
    



    /* -- PROTECTED METHODS ------------------------------------------------ */
    
    @Override
    protected ScrapedRecipe scrapeFromElement(Element element)
    {   
        ScrapedRecipe result = null;
        
        if(element.select("a").size() < 2)
        {
            Logger.error(NosaltyVisitor.class.getName() + ".scrapeFromElement(): missing recipe link.");
        }
        
        Element recipeA = element.select("a").get(1);
        
        String recipeName = recipeA.text().trim();
        String recipeUrl  = recipeA.attr("abs:href");
        
        Logger.info(NosaltyVisitor.class.getName() + ".scrapeFromElement(): Scraping recipe.\n" +
            "   recipeUrl  = " + recipeUrl + "\n" +
            "   recipeName = " + recipeName);
        
        try
        {
            result = scrapeRecipeFromUrl(recipeUrl);
            
            if(result == null)
            {
                /* Log problem. */
                Logger.error(NosaltyVisitor.class.getName() + ".scrapeFromElement(): Data for ScrapedRecipe is not OK!");
            }
        }
        catch(IOException e)
        {
            Logger.error(NosaltyVisitor.class.getName() + ".scrapeFromElement(): Failed to connect to recipe URL!\n" +
                "    recipeUrl = " + recipeUrl);
            
            e.printStackTrace();
        }
        
        return result;
    }

    @Override
    protected void goToNextPage() throws Exception
    {
        /*
         * Page structure as of 14.07.2015.:
         * 
         * <div class="article-list article-list-horizontal" data-col="5">
         *     ...
         *     <ul>
         *         <li>[RECIPE ELEMENT_1]</li>
         *         ...
         *         <li>[RECIPE ELEMENT_n]</li>
         *         ...
         *     </ul>
         *     ...
         * </div>
         */
        
        Logger.debug(NosaltyVisitor.class.getName() + ".goToNextPage():\n" +
            "    pageCounter = " + pageCounter);
        
        /* Get the page of recipes. */
        connection = Jsoup.connect(String.format(BASE_URL, pageCounter));
        mainPage   = connection.get();
        
        recipeElements = mainPage.select(RECIPE_MAIN_CONTENT_SELECTOR).get(0).select("li");
        
        ScrapedRecipe firstRecipe = scrapeFromElement(recipeElements.get(0));
        
        if(firstRecipe.getUrl().equals(firstRecipeUrl))
        {
            /* Last page reached. */
            recipeElements = new ArrayList<Element>();
            
            Logger.info(NosaltyVisitor.class.getName() + ".goToNextPage(): Reached last page!");
        }
        
        if(recipeElements.size() > 0)
        {
            /* Save first URL. */
            firstRecipeUrl = firstRecipe.getUrl();
            
            /* Set page counter to next. */
            pageCounter++;
        }
    }



    /* -- PRIVATE METHODS -------------------------------------------------- */
    
    /**
     * Scrapes ingredients.
     * 
     * @param recipeDocument    The recipe document.
     * 
     * @return The list of scraped ingredients.
     */
    private static List<ScrapedIngredient> scrapeIngredients(Document recipeDocument)
    {
        List<ScrapedIngredient> result = null;
        
        List<Element> ingredientElems = recipeDocument.select(".recept-hozzavalok").select("li");
        
        for(Element ingredientElem: ingredientElems)
        {
            if(result == null)
            {
                result = new ArrayList<ScrapedIngredient>();
            }
            
            String allText        = ingredientElem.text();
            String ingredientText = "";
            
            Elements ingredients = ingredientElem.select("a");
            if(ingredients.size() == 1)
            {
                ingredientText = ingredients.get(0).text();
            }
            else
            {
                /* Go through the elements until an "<a>" with not null text is found. */
                for(Element elem: ingredients)
                {
                    if(elem.text().length() > 0)
                    {
                        ingredientText = elem.text();
                        
                        break;
                    }
                }
            }
            
            if(ingredientText.length() == 0)
            {
                /* 
                 * If ingredientText is still empty, then there's probably no "<a>".
                 * In this case, assume the first two words belong to measure and amount, and
                 * the rest is ingredient text.
                 */
                String[] ingWords = allText.split(" ");
                
                /* If length > 2, join ingredient text parts. */
                if(ingWords != null && ingWords.length > 2)
                {
                    for(int i = 2; i < ingWords.length - 1; i++)
                    {
                        ingredientText += ingWords[i] + " ";
                    }
                }
                
                if(ingWords != null)
                {
                    ingredientText += ingWords[ingWords.length - 1];
                }
            }
            
            Logger.debug(NosaltyVisitor.class.getName() + ".scrapeIngredients():\n" + 
                "    allText        = " + allText  + "\n" +
                "    ingredientText = " + ingredientText);
            
            if(ingredientText.length() > 0)
            {
                /* 
                 * Extract amount and measure info.
                 * cases:
                 *   - no amount and measure at all. -> default amount and measure is used.
                 *   - only 1 word of amount and measures -> we'll assume it's the amount
                 *   - normal case
                 */
                String[] amountMeasureTexts = allText.split(ingredientText);
                
                String amountMeasureText = null;
                
                if(amountMeasureTexts != null && amountMeasureTexts.length > 0)
                {
                    amountMeasureText = amountMeasureTexts[0];
                }
                
                String[] amountMeasures = null;
                
                if(amountMeasureText != null)
                {
                    amountMeasures = amountMeasureText.split(" ");
                }
                
                String amountText  = "0";
                String measureText = "to taste";
                
                if(amountMeasures != null)
                {
                    if(amountMeasures.length > 1)
                    {
                        amountText  = amountMeasures[0].trim();
                        measureText = amountMeasures[1].trim();
                    }
                    else if(amountMeasures.length == 1)
                    {
                        /* Assume that only amount is there. */
                        amountText = amountMeasures[0].trim();
                        
                        /* Assume piece. */
                        measureText = "pc";
                    }
                }
                
                Double amount = 0.0;
                
                try
                {
                    amount = Double.parseDouble(amountText);
                }
                catch(NumberFormatException e)
                {
                    Logger.warn(NosaltyVisitor.class.getName() + ".scrapeIngredients(): Failed to parse amount!\n" +
                        "   amountText = " + amountText);
                }
                
                ScrapedIngredient scraped = new ScrapedIngredient(ingredientText, measureText, amount);
                
                result.add(scraped);
            }
        }
        
        return result;
    }
    
    /**
     * Scrapes the servings.
     * 
     * @param recipeDocument    The recipe document.
     * 
     * @return The scraped servings.
     */
    private static int scrapeServings(Document recipeDocument)
    {
        int result = 0;
        
        Element servingsElem = recipeDocument.select(".recept-hozzavalok > h2").select("span").first();
        
        String servingText = servingsElem.text();
        
        /* Remove textual part. */
        servingText = servingText.split(" ")[0];
        
        /* Parsing. */
        try
        {
            result = Integer.parseInt(servingText);
        }
        catch(NumberFormatException e)
        {
            Logger.warn(NosaltyVisitor.class.getName() + "scrapeServings(): Failed to parse servings. Using 0.");
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
