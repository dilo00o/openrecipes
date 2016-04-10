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

package scrapers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import play.Logger;
import scrapers.data.ScrapedIngredient;

/**
 * Scraper class for ingredients.
 *
 * @author Oliver Dozsa
 */
public class IngredientScraper
{
    /* --------------------------------------------------------------------- */
    /* ATTRIBUTES                                                            */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC ATTRIBUTES ------------------------------------------------ */



    /* -- PROTECTED ATTRIBUTES --------------------------------------------- */



    /* -- PRIVATE ATTRIBUTES ----------------------------------------------- */
    
    /**
     * The list of ingredients.
     */
    private List<ScrapedIngredient> scrapedIngredients;
    
    /**
     * The source pages.
     */
    private static String[] sourcePages = 
    {
        "http://www.xn--kalriaguru-ibb.hu/kaloriatablazat/pekaruk-kaloriatablazat.php",
        "http://www.xn--kalriaguru-ibb.hu/kaloriatablazat/etelizesitok-es-hozzavalok-kaloriatablazat.php",
        "http://www.xn--kalriaguru-ibb.hu/kaloriatablazat/gabonatermekek-kaloriatablazat.php",
        "http://www.xn--kalriaguru-ibb.hu/kaloriatablazat/szoszok-ontetek-kremek-kaloriatablazat.php",
        "http://www.xn--kalriaguru-ibb.hu/kaloriatablazat/tojas-kaloriatablazat.php",
        "http://www.xn--kalriaguru-ibb.hu/kaloriatablazat/fuszerek-kaloriatablazat.php",
        "http://www.xn--kalriaguru-ibb.hu/kaloriatablazat/tesztak-kaloriatablazat.php",
        "http://www.xn--kalriaguru-ibb.hu/kaloriatablazat/barany-kaloriatablazat.php",
        "http://www.xn--kalriaguru-ibb.hu/kaloriatablazat/gyumolcsok-es-gyumolcskeszitmenyek-kaloriatablazat.php",
        "http://www.xn--kalriaguru-ibb.hu/kaloriatablazat/borju-kaloriatablazat.php",
        "http://www.xn--kalriaguru-ibb.hu/kaloriatablazat/zoldsegek-es-huvelyesek-kaloriatablazat.php",
        "http://www.xn--kalriaguru-ibb.hu/kaloriatablazat/halak-es-tenger-gyumolcsei-kaloriatablazat.php",
        "http://www.xn--kalriaguru-ibb.hu/kaloriatablazat/diofelek-es-olajos-magvak-kaloriatablazat.php",
        "http://www.xn--kalriaguru-ibb.hu/kaloriatablazat/huskeszitmenyek-kaloriatablazat.php",
        "http://www.xn--kalriaguru-ibb.hu/kaloriatablazat/zsirok-es-olajok-kaloriatablazat.php",
        "http://www.xn--kalriaguru-ibb.hu/kaloriatablazat/marha-kaloriatablazat.php",
        "http://www.xn--kalriaguru-ibb.hu/kaloriatablazat/gombak-kaloriatablazat.php",
        "http://www.xn--kalriaguru-ibb.hu/kaloriatablazat/sertes-kaloriatablazat.php",
        "http://www.xn--kalriaguru-ibb.hu/kaloriatablazat/szoja-kaloriatablazat.php",
        "http://www.xn--kalriaguru-ibb.hu/kaloriatablazat/szarnyasok-kaloriatablazat.php",
        "http://www.xn--kalriaguru-ibb.hu/kaloriatablazat/alkoholos-italok-kaloriatablazat.php",
        "http://www.xn--kalriaguru-ibb.hu/kaloriatablazat/vad-es-egyeb-husok-kaloriatablazat.php",
        "http://www.xn--kalriaguru-ibb.hu/kaloriatablazat/kave-tea-kakao-kaloriatablazat.php"
    };



    /* --------------------------------------------------------------------- */
    /* METHODS                                                               */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC METHODS --------------------------------------------------- */
    
    /**
     * Constructor.
     */
    public IngredientScraper()
    {
        scrapedIngredients = new ArrayList<ScrapedIngredient>();
    }
    
    public List<ScrapedIngredient> getScrapedIngredients()
    {
        if(scrapedIngredients.size() == 0)
        {
            for(String sourcePage: sourcePages)
            {
                Logger.debug(IngredientScraper.class.getName() + ".getScrapedIngredients()\n" +
                    "    parsing from: " + sourcePage
                );
                
                List<ScrapedIngredient> scrapedIngsFromPage = getIngredientsFromPage(sourcePage);
                
                Logger.debug(IngredientScraper.class.getName() + ".getScrapedIngredients()\n" +
                    "    parsed ingredients: " + scrapedIngsFromPage.size()
                );
                
                scrapedIngredients.addAll(scrapedIngsFromPage);
            }
        }
        
        return scrapedIngredients;
    }



    /* -- PROTECTED METHODS ------------------------------------------------ */



    /* -- PRIVATE METHODS -------------------------------------------------- */
    
    /**
     * Scrapes ingredient from the given page (from site "kaloriaguru").
     * 
     * @param url    The url.
     * @return The scraped ingredients.
     */
    private List<ScrapedIngredient> getIngredientsFromPage(String url)
    {
        List<ScrapedIngredient> result = new ArrayList<ScrapedIngredient>();
        
        try
        {
            Document document = Jsoup.connect(url).userAgent("Mozilla").get();
            
            List<Element> rows = document.select("table.calorieTable").select("tbody").select("tr");
            
            for(Element row: rows)
            {
                String ingName = row.select("td").first().text();
                
                ScrapedIngredient scrIng = new ScrapedIngredient(ingName.toLowerCase(), "na", 0.0);
                
                result.add(scrIng);
            }
            
            Logger.debug(IngredientScraper.class.getName() + ".getIngredientsFromPage()\n" +
                "    result.size() = " + result.size()
            );
        }
        catch (IOException e)
        {
            e.printStackTrace();
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
