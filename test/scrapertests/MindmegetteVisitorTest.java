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

package scrapertests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import play.Logger;
import scrapers.data.ScrapedRecipe;
import scrapers.visitors.MindmegetteVisitor;

/**
 * Test class for mindmegette visitor.
 *
 * @author Oliver Dozsa
 */
public class MindmegetteVisitorTest
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
    private MindmegetteVisitor visitor;



    /* --------------------------------------------------------------------- */
    /* METHODS                                                               */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC METHODS --------------------------------------------------- */
    
    @Before
    public void setup()
    {
        visitor = new MindmegetteVisitor();
    }
    
    /**
     * Test the initialization of the visitor.
     */
    @Test
    public void testConnection()
    {
        assertNotNull(visitor);

        assertTrue(visitor.isConnected());
    }

    /**
     * Tests scraping a few recipes.
     */
    @Test
    public void testScraping()
    {
        /* The number of recipes to scrape to make sure there's at least one "paging". */
        int n = 60;

        for(int i = 0; i < n; i++)
        {
            if(visitor.hasMoreElements())
            {
                ScrapedRecipe recipe = visitor.nextElement();
                
                if(recipe == null)
                {
                    Logger.error("recipe is null!");
                }
                else
                {
                    Logger.info(recipe.toString());
                }                
            }
        }
    }



    /* -- PROTECTED METHODS ------------------------------------------------ */



    /* -- PRIVATE METHODS -------------------------------------------------- */



    /* --------------------------------------------------------------------- */
    /* OTHERS                                                                */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC OTHERS ---------------------------------------------------- */



    /* -- PROTECTED OTHERS ------------------------------------------------- */



    /* -- PRIVATE OTHERS --------------------------------------------------- */
}
