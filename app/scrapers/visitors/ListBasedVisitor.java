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

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;

import play.Logger;
import scrapers.data.ScrapedRecipe;

/**
 * Template class.
 *
 * @author Oliver Dozsa
 */
public abstract class ListBasedVisitor extends RecipeSiteVisitor
{    
    /* --------------------------------------------------------------------- */
    /* ATTRIBUTES                                                            */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC ATTRIBUTES ------------------------------------------------ */



    /* -- PROTECTED ATTRIBUTES --------------------------------------------- */
    
    /**
     * Points to the current element to scrape.
     */
    protected int elementCounter;
    
    /**
     * Points to the current page.
     */
    protected int pageCounter;
    
    /**
     * The recipe elements of the current page.
     */
    protected List<Element> recipeElements;



    /* -- PRIVATE ATTRIBUTES ----------------------------------------------- */
    
    /**
     * Flag to check whether there are more elements to get.
     */
    private boolean hasMoreElements;



    /* --------------------------------------------------------------------- */
    /* METHODS                                                               */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC METHODS --------------------------------------------------- */
    
    /**
     * Initializes the list based visitor with the given id.
     * 
     * @param id    The id.
     * */
    public ListBasedVisitor(int id)
    {
        super(id);
        
        elementCounter  = 0;
        pageCounter     = 0;
        hasMoreElements = true;
        
        recipeElements = new ArrayList<Element>();
        
        changeState(VisitorState.WORK);
        
        try
        {
            goToNextPage();
        }
        catch(Exception e)
        {
            Logger.error(this.getClass().getName() + "." + this.getClass().getName() + "(): Failed to go to first page!");
            
            changeState(VisitorState.ERROR);
            
            errorCode = ErrorCode.ERROR_INIT;
            
            e.printStackTrace();
        }
    }
    
    /**
     * Checks if there are more elements to get.
     * 
     * @see java.util.Enumeration#hasMoreElements()
     */
    @Override
    public boolean hasMoreElements()
    {
        boolean result = false;
        
        switch(getState())
        {
            case WORK:
            {
                result = hasMoreElements;
                
                break;
            }
            
            case INIT:
            case ERROR:
            default:
            {
                break;
            }
        }
        
        return result;
    }

    /**
     * Scrapes the next element.
     * 
     * @return The next element. Null is returned if there are no more recipes to scrape, or if there was an error.
     * */
    @Override
    public ScrapedRecipe nextElement()
    {
        Logger.debug(this.getClass().getName() + ".nextElement()\n" +
            "    elementCounter        = " + elementCounter + "\n" +
            "    recipeElements.size() = " + recipeElements.size() + "\n" +
            "    pageCounter           = " + pageCounter + "\n" +
            "    hasMoreElements()     = " + hasMoreElements()
        );
        
        ScrapedRecipe result = null;
        
        switch(getState())
        {
            case WORK:
            {
                if(elementCounter >= recipeElements.size())
                {
                    /* Reached last element, try to go to next page. */
                    try
                    {
                        goToNextPage();
                        
                        if(recipeElements.isEmpty())
                        {
                            /* Processed last page, no more elements to get. */
                            hasMoreElements = false;
                        }
                        else
                        {
                            /* Reset elementCounter. */
                            elementCounter = 0;
                        }
                    }
                    catch (Exception e)
                    {
                        Logger.error(this.getClass().getName() + ".nextElement(): Failed to go to next page!\n" + 
                            "    e  = " + e);
                        
                        changeState(VisitorState.ERROR);
                        
                        errorCode = ErrorCode.ERROR_PAGE_LOAD;
                    }
                }
                
                /* Check for more elements, and possible errors. */
                if(hasMoreElements && getState() == VisitorState.WORK)
                {
                    /* Scrape the current element. */
                    try
                    {
                        result = scrapeFromElement(recipeElements.get(elementCounter));
                    }
                    catch(Exception e)
                    {
                        Logger.error(this.getClass().getName() + ".nextElement(): Exception in scrapeFromElement()!\n" +
                            "    e = " + e);
                        
                        e.printStackTrace();
                    }
                    
                    if(result == null)
                    {
                        Logger.error(this.getClass().getName() + ".nextElement(): Failed to scrape recipe!");
                        
                        /* Failed to scrape, go to error state. */
                        changeState(VisitorState.ERROR);
                        
                        errorCode = ErrorCode.ERROR_SCRAPE;
                    }
                    else
                    {
                        /* Scraping is successful, point to next recipe. */
                        elementCounter++;
                    }
                }
                
                break;
            }
            
            case INIT:
            case ERROR:
            default:
            {   
                break;
            }
        }
        
        return result;
    }
    
    @Override
    public void skipCurrentElement()
    {
        elementCounter++;
    }
    
    @Override
    public void skipCurrentPage()
    {
        pageCounter++;
        elementCounter = 0;
        recipeElements = new ArrayList<Element>();
    }
    
    /**
     * Sets the page counter.
     * 
     * @param pageCounter    The page counter.
     */
    public void setToPage(int pageCounter)
    {
        this.pageCounter = pageCounter;
        elementCounter = 0;
        recipeElements = new ArrayList<Element>();
    }



    /* -- PROTECTED METHODS ------------------------------------------------ */
    
    /**
     * Scrape a recipe from the given element.
     * 
     * @param element    The element to scrape from.
     * 
     * @return The scraped recipe. Null should be returned if scraping fails.
     */
    protected abstract ScrapedRecipe scrapeFromElement(Element element);
    
    /**
     * Should go to the next page and set recipeElements.
     */
    protected abstract void goToNextPage() throws Exception;
    
    

    /* -- PRIVATE METHODS -------------------------------------------------- */



    /* --------------------------------------------------------------------- */
    /* OTHERS                                                                */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC OTHERS ---------------------------------------------------- */



    /* -- PROTECTED OTHERS ------------------------------------------------- */



    /* -- PRIVATE OTHERS --------------------------------------------------- */
    
}
