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

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import play.Logger;
import scrapers.IngredientScraper;
import scrapers.data.ScrapedIngredient;

/**
 * Unit testing ingredient scraper.
 *
 * @author Oliver Dozsa
 */
public class IngredientScraperTest
{
    /* --------------------------------------------------------------------- */
    /* ATTRIBUTES                                                            */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC ATTRIBUTES ------------------------------------------------ */



    /* -- PROTECTED ATTRIBUTES --------------------------------------------- */



    /* -- PRIVATE ATTRIBUTES ----------------------------------------------- */



    /* --------------------------------------------------------------------- */
    /* METHODS                                                               */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC METHODS --------------------------------------------------- */
    
    @Test
    public void testScraping()
    {
        IngredientScraper ingScraper = new IngredientScraper();
        
        List<ScrapedIngredient> scrapedIngs = ingScraper.getScrapedIngredients();
        
        assertNotNull("scrapedIngs is null!", scrapedIngs);
        
        assertTrue("scrapedIngs's size is 0!", scrapedIngs.size() > 0);
        
        /* Just log. */
        for(ScrapedIngredient scrapedIng: scrapedIngs)
        {
            Logger.info("scrpd ing = " + scrapedIng.getName());
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
