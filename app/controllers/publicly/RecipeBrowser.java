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

package controllers.publicly;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

import views.html.publicviews.recipebrowser.*;

/**
 * Template class.
 *
 * @author Oliver Dozsa
 */
public class RecipeBrowser extends Controller
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
    
    /**
     * The main page for recipe browser.
     *
     * @return The main page for recipe browser.
     * */
    public Result searchByIngredients()
    {
        Logger.debug(RecipeBrowser.class.getName() + ".searchByIngredients()");

        Result result = null;

        result = ok(searchByIngredients.render());
        
        return result;
    }
    
    /**
     * Search for recipes based on properties.
     *
     * @return Page for search by recipe properties.
     * */
    public Result searchByRecipeProperties()
    {
        Logger.debug(RecipeBrowser.class.getName() + ".searchByRecipeProperties()");

        Result result = null;

        result = ok(searchByRecipeProperties.render());
        
        return result;
    }



    /* -- PROTECTED METHODS ------------------------------------------------ */



    /* -- PRIVATE METHODS -------------------------------------------------- */



    /* --------------------------------------------------------------------- */
    /* OTHERS                                                                */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC OTHERS ---------------------------------------------------- */
    
    /**
     * Enum representing search mode.
     * */
    public static enum SearchMode
    {
        EXACT       (1),
        AT_LEAST    (2),
        ANY_OF      (3),
        GROUP       (4),

        UNKNOWN     (9);

        /**
         * The id.
         * */
        private int id;

        /**
         * Creates a search mode with id.
         *
         * @param id    The id.
         * */
        private SearchMode(int id)
        {
            this.id   = id;
        }

        /**
         * Gets the id.
         *
         * @return The id.
         * */
        public int getId()
        {
            return id;
        }

        /**
         * Creates a search mode from it's integer representation.
         *
         * @return The search mode, or SearchMode.UNKNOWN, if converting
         * fails.
         * */
        public static SearchMode fromInt(Integer id)
        {
            SearchMode result = SearchMode.UNKNOWN;

            switch(id)
            {
                case 1:
                {
                    result = SearchMode.EXACT;

                    break;
                }

                case 2:
                {
                    result = SearchMode.AT_LEAST;

                    break;
                }

                case 3:
                {
                    result = SearchMode.ANY_OF;

                    break;
                }

                case 4:
                {
                    result = SearchMode.GROUP;

                    break;
                }

                default:
                {
                    /* Nothing should be done. */

                    break;
                }
            }

            return result;
        }
    }

    /**
     * Enum representing the search type.
     * */
    public static enum SearchType
    {
        BY_RECIPE_PROPERTIES    (1),
        BY_INGREDIENTS          (2),
        UNKNOWN                 (0);

        /**
         * The id.
         * */
        private Integer id;

        /**
         * Creates a search type with id.
         *
         * @param id    The id.
         * */
        private SearchType(int id)
        {
            this.id = id;
        }

        /**
         * Gets the id.
         *
         * @return The id.
         * */
        public Integer getId()
        {
            return id;
        }
    }



    /* -- PROTECTED OTHERS ------------------------------------------------- */



    /* -- PRIVATE OTHERS --------------------------------------------------- */
}
