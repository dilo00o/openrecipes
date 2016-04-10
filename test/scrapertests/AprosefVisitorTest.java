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

import org.junit.Before;
import org.junit.Test;
import play.Logger;
import scrapers.data.ScrapedRecipe;
import scrapers.visitors.AprosefVisitor;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * For testing the Aprosef visitor.
 *
 * @author Oliver Dozsa
 */
public class AprosefVisitorTest
{
    /* --------------------------------------------------------------------- */
    /* ATTRIBUTES                                                            */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC ATTRIBUTES ------------------------------------------------ */



    /* -- PROTECTED ATTRIBUTES --------------------------------------------- */



    /* -- PRIVATE ATTRIBUTES ----------------------------------------------- */

    /**
     * The visitor.
     * */
    private AprosefVisitor asVisitor;



    /* --------------------------------------------------------------------- */
    /* METHODS                                                               */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC METHODS --------------------------------------------------- */

    /**
     * Used to initialize what's needed for tests.
     */
    @Before
    public void setup()
    {
        asVisitor = new AprosefVisitor();
    }

    /**
     * Test the initialization of the visitor.
     */
    @Test
    public void testConnection()
    {
        assertNotNull(asVisitor);

        assertTrue(asVisitor.isConnected());
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
            if(asVisitor.hasMoreElements())
            {
                ScrapedRecipe recipe = asVisitor.nextElement();

                Logger.info(recipe.toString());
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
