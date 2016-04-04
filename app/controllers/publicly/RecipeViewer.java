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

import com.avaje.ebean.PagedList;
import com.avaje.ebean.Query;
import models.*;
import models.IngredientName;
import models.Recipe;
import models.RecipeIngredient;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Result;
import play.mvc.Controller;

import views.html.publicviews.recipeview.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Recipe viewer.
 *
 * @author Oliver Dozsa
 */
public class RecipeViewer extends Controller
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
     * Based on recipe query, it renders the compact view of recipes.
     *
     * @param query              The query to use.
     * @param page               The page index.
     * @param sortOrder          The sort order (ASC, or DESC).
     * @param sortBy             The sort by property (e.g. recipe name).
     * @param pageSize           The page size.
     * @param searchType         The type of the search.
     * @param searchParamKeys    The search parameters.
     *
     * @return Compact view of recipes.
     * */
    public static Result searchResults
    (
        Query<Recipe> query,
        Integer page,
        String sortOrder,
        String sortBy,
        Integer pageSize,
        RecipeBrowser.SearchType searchType,
        Map<String, String> searchParamKeys
    )
    {
        Logger.debug(RecipeViewer.class.getName() + ".searchResults()\n" +
            "    page            = " + page + "\n" +
            "    sortOrder       = " + sortOrder + "\n" +
            "    sortBy          = " + sortBy + "\n" +
            "    pageSize        = " + pageSize + "\n" +
            "    searchType      = " + searchType + "\n" +
            "    searchParamKeys = " + searchParamKeys
        );

        Result result = null;

        /* Check for errors. */
        boolean isError = checkParams_searchResults
        (
            query,
            page,
            sortOrder,
            sortBy,
            pageSize,
            searchType,
            searchParamKeys
        );

        if(!isError)
        {
            Map<Long, String> recipesNames           = new HashMap<Long, String>();
            Map<Long, Integer> recipeskCalPerPortion = new HashMap<Long, Integer>();
            String usedSortOrder                     = "asc";

            /* Check sort order. In case it's not valid, we'll use the default "asc" */
            if(sortOrder != null)
            {
                if(sortOrder.equalsIgnoreCase("asc") || sortOrder.equalsIgnoreCase("desc"))
                {
                    usedSortOrder = sortOrder;
                }
            }

            if(sortBy != null)
            {
                if(!sortBy.equalsIgnoreCase(""))
                {
                    /* We assume, that sortBy points to a valid column, if present. If not, an error page is returned. */
                    query.orderBy(sortBy + " " + usedSortOrder);
                }
            }

            /* Get the result. */
            PagedList<Recipe> recipePage = query
                .findPagedList(page, pageSize);
            
            Logger.info(RecipeViewer.class.getName() + " recipePage.getTotalRowCount(): " + recipePage.getTotalRowCount());
            
            Logger.info(RecipeViewer.class.getName() + " recipePage.getList().size(): " + recipePage.getList().size());

            if(recipePage != null)
            {
                for(Recipe recipe: recipePage.getList())
                {
                    recipesNames.put(recipe.id, recipe.name);
                }

                result = ok
                (
                    compact.render
                    (
                        recipePage,
                        recipesNames,
                        page,
                        sortBy,
                        sortOrder,
                        searchType,
                        searchParamKeys
                    )
                );
            }
            else
            {
                result = badRequest("No results to show!");
            }
        }
        else
        {
            Logger.error(RecipeViewer.class.getName() + ".searchResults(): Parameters are not ok!");

            // TODO: error code?
            result = badRequest("Error while searching for recipes!");
        }

        return result;
    }



    /* -- PROTECTED METHODS ------------------------------------------------ */



    /* -- PRIVATE METHODS -------------------------------------------------- */

    /**
     * Converts a string to Integer. If conversion fails,
     * returns 0.
     *
     * @return The converted integer.
     * */
    private static Integer parseInteger(String intStr)
    {
        Logger.debug(RecipeViewer.class.getName() + ".parseInteger(): intStr = " + intStr);

        Integer result = 0;

        try
        {
            result = Integer.parseInt(intStr);
        }
        catch(NumberFormatException e)
        {
            Logger.warn(RecipeViewer.class.getName() + ".parseInteger():\n" +
                "    NumberFormatException! intStr = " + intStr);
        }
        catch(Exception e)
        {
            Logger.warn(RecipeViewer.class.getName() + ".parseInteger():\n" +
                "    Exception! intStr = " + intStr);
        }

        return result;
    }

    /**
     * Converts a string to Long. If conversion fails,
     * returns 0.
     *
     * @return The converted long.
     * */
    private static Long parseLongNum(String longStr)
    {
        Logger.debug(RecipeViewer.class.getName() + ".parseLongNum(): longStr = " + longStr);

        Long result = 0L;

        try
        {
            result = Long.parseLong(longStr);
        }
        catch(NumberFormatException e)
        {
            Logger.warn(RecipeViewer.class.getName() + ".parseLongNum():\n" +
                "    NumberFormatException! longStr = " + longStr);
        }
        catch(Exception e)
        {
            Logger.warn(RecipeViewer.class.getName() + ".parseLongNum():\n" +
                "    Exception! longStr = " + longStr);
        }

        return result;
    }

    /**
     * Checks the parameters of the searchResults() method.
     *
     * @param query              The query to use.
     * @param page               The page index.
     * @param sortOrder          The sort order (ASC, or DESC).
     * @param sortBy             The sort by property (e.g. recipe name).
     * @param pageSize           The page size.
     * @param language           The language used for recipe names.
     * @param rTagsLanguage      The used language for the recipe tags.
     * @param searchType         The type of the search.
     * @param searchParamKeys    The search parameters.
     *
     * @return True if an error was detected with the params.
     * */
    private static boolean checkParams_searchResults
    (
        Query<Recipe> query,
        Integer page,
        String sortOrder,
        String sortBy,
        Integer pageSize,
        RecipeBrowser.SearchType searchType,
        Map<String, String> searchParamKeys
    )
    {
        boolean result = false;

        /* sortOrder */
        if(sortOrder != null)
        {
            if(sortOrder.equalsIgnoreCase("asc") || sortOrder.equalsIgnoreCase("desc"))
            {
                /* Received valid sort order. */
            }
            else
            {
                /* Sort order is invalid. */
                Logger.warn(RecipeViewer.class.getName() + ".checkParams_searchResults(): Invalid sort order!\n" +
                    "    sortOrder = " + sortOrder
                );

                /* Don't trigger an error here as there's a default value for sort order. */
            }
        }
        else
        {
            /* Don't trigger an error here as there's a default value for sort order. */

            Logger.warn(RecipeViewer.class.getName() + ".checkParams_searchResults(): sortOrder is null!");
        }


        /* sortBy */
        if(sortBy != null)
        {
            if(sortBy.equalsIgnoreCase(""))
            {
                /* Don't trigger error as by default, sorting won't be used if something's not ok. */
                Logger.warn(RecipeViewer.class.getName() + ".checkParams_searchResults(): sortBy is empty!");
            }
        }
        else
        {
            /* Don't trigger error as by default, sorting won't be used if something's not ok. */
            Logger.warn(RecipeViewer.class.getName() + ".checkParams_searchResults(): sortBy is null!");
        }


        /* page */
        if(page == null)
        {
            Logger.error(RecipeViewer.class.getName() + ".checkParams_searchResults(): page is null!");

            result = true;
        }
        else if(page < 0)
        {
            /* If page is negative, it's an error. */
            Logger.error(RecipeViewer.class.getName() + ".checkParams_searchResults(): page is < 0! \n" +
                "    page = " + page);
        }


        /* pageSize */
        if(pageSize == null)
        {
            Logger.error(RecipeViewer.class.getName() + ".checkParams_searchResults(): pageSize is null!");

            result = true;
        }
        else if(pageSize < 0)
        {
            /* If pagesize is negative, it's an error. */
            Logger.error(RecipeViewer.class.getName() + ".checkParams_searchResults(): pageSize is < 0! \n" +
                "    pageSize = " + pageSize);

            result = true;
        }


        /* searchType */
        if(searchType == null)
        {
            Logger.error(RecipeViewer.class.getName() + ".checkParams_searchResults(): searchType is null!");

            result = true;
        }
        else if(searchType == RecipeBrowser.SearchType.UNKNOWN)
        {
            Logger.error(RecipeViewer.class.getName() + ".checkParams_searchResults(): searchType is unknown!");

            result = true;
        }

        if(searchParamKeys == null)
        {
            Logger.error(RecipeViewer.class.getName() + ".checkParams_searchResults(): searchParamKeys is null!");

            result = true;
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
