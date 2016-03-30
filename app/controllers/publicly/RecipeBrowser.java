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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import models.Ingredient;
import models.IngredientName;
import models.IngredientTag;
import play.Logger;
import play.libs.Json;
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
     * Executes the search based on included / excluded ingredients.
     *
     * @return The result page (found recipes).
     * */
    public Result exec_searchByIngredients()
    {
        Logger.debug(RecipeBrowser.class.getName() + ".exec_searchByIngredients():\n");

        Result result = null;

        /* TODO */
        result = searchByIngredients();

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
    
    /**
     * Used for getting ingredients (via AJAX) for searching by name, and language.
     * 
     * @param query         The query string.
     * @param languageID    The id of the language in which the search should be performed.
     * 
     * @return The available ingredient names as a json object, according to typeahead format.
     * */
    public Result ingredients(String query, Long languageID)
    {
        Logger.debug(RecipeBrowser.class.getName() + ".ingredients():\n" +
            "    query      = " + query + "\n" +
            "    languageID = " + languageID
        );

        Result result = null;

        ArrayNode jsonResults = Json.newObject().arrayNode();

        if(query != null && languageID != null)
        {
            if(query.length() > 0)
            {
                List<Ingredient> ingredients = Ingredient.getIngredientsLikeByLanguage(query, languageID);

                for(Ingredient ingredient: ingredients)
                {
                    IngredientName ingName = ingredient.getNameByLanguage(languageID);

                    if(ingName != null && ingName.name != null)
                    {
                        ObjectNode queryJsonResult = Json.newObject();

                        queryJsonResult.put("value", ingName.name);
                        queryJsonResult.put("id", ingredient.id);

                        ArrayNode tokens = queryJsonResult.putArray("tokens");
                        tokens.add(ingName.name);

                        jsonResults.add(queryJsonResult);
                    }
                    else
                    {
                        if(ingName == null)
                        {
                            Logger.warn(RecipeBrowser.class.getName() + ".ingredients(): ingName is null!");
                        }
                        else
                        {
                            Logger.error(RecipeBrowser.class.getName() + ".ingredients(): ingName's name is null!\n" +
                                "    ingName.id = " + ingName.id);
                        }
                    }
                }
            }
        }

        result = ok(jsonResults);
        
        return result;
    }
    
    /**
     * Used for getting ingredient tags (via AJAX) for searching.
     * 
     * @param query             The query string.
     * 
     * @return The available ingredient tags as a json object, according to typeahead format.
     * */
    public Result ingredientTags(String query)
    {
        Logger.debug(RecipeBrowser.class.getName() + ".ingredientTags():\n" +
            "    query = " + query
        );

        Result result = null;
        
        ArrayNode jsonResults = Json.newObject().arrayNode();

        if(query != null)
        {
            if(query.length() > 0)
            {
                List<IngredientTag> tags = IngredientTag.getTagsByNameLike(query);

                for(IngredientTag tag: tags)
                {
                    ObjectNode queryJsonResult = Json.newObject();

                    /* We assume that name cannot be null (db restriction). */
                    queryJsonResult.put("value", tag.name);
                    queryJsonResult.put("id", tag.id);

                    ArrayNode tokens = queryJsonResult.putArray("tokens");
                    tokens.add(tag.name);

                    jsonResults.add(queryJsonResult);
                }
            }
        }
        else
        {
            Logger.warn(RecipeBrowser.class.getName() + ".ingredientTags(): query is null!");
        }
        
        result = ok(jsonResults);
        
        return result;
    }
    
    /**
     * Used for getting ingredients (AJAX) with given tags, and other restrictions.
     *
     * @param tagIDs        The ids of the tags (comma separated string).
     * @param isAND         If set to true, there'll be an AND relation between the tags (by default, they are OR-ed.)
     * @param languageID    The id of the language in which the search should be performed.
     *
     * @return The result ingredients as json.
     * */
    public Result ingredientsWithTags
    (
        String tagIDs,
        boolean isAND,
        Long languageID
    )
    {
        Logger.debug(RecipeBrowser.class.getName() + ".ingredientsWithTags():\n" +
            "    tagIDs     = " + tagIDs     + "\n" +
            "    isAND      = " + isAND      + "\n" +
            "    languageID = " + languageID
        );

        Result result = null;
        
        ArrayNode jsonResults = Json.newObject().arrayNode();

        boolean isParamsValid = true;

        /* Check for parameters validity. */
        if(tagIDs     == null || languageID == null)
        {
            isParamsValid = false;
        }
        else
        {
            Logger.warn(RecipeBrowser.class.getName() + ".ingredientsWithTags(): params are not valid!");
        }

        if(isParamsValid)
        {
            String[] tagsIDsArray = tagIDs.split(",");

            List<IngredientTag> tags = new ArrayList<IngredientTag>();

            for(String tagIDStr: tagsIDsArray)
            {
                Long tagID        = Long.parseLong(tagIDStr);
                IngredientTag tag = IngredientTag.find.byId(tagID);

                if(tag != null)
                {
                    tags.add(tag);
                }
            }

            List<Ingredient> ingredients;

            ingredients = Ingredient.getIngredientsWithTags
            (
                tags,
                isAND
            );

            for(Ingredient ingredient: ingredients)
            {
                IngredientName ingName = ingredient.getNameByLanguage(languageID);

                if(ingName != null && ingName.name != null)
                {
                    ObjectNode jsonResult = Json.newObject();

                    jsonResult.put("id", ingredient.id);
                    jsonResult.put("name", ingName.name);

                    jsonResults.add(jsonResult);
                }
                else
                {
                    if(ingName == null)
                    {
                        Logger.warn(RecipeBrowser.class.getName() + ".ingredientsWithTags(): ingName is null!");
                    }
                    else
                    {
                        Logger.error(RecipeBrowser.class.getName() + ".ingredientsWithTags(): ingName's name is null!\n" +
                            "    ingName.id = " + ingName.id
                        );
                    }
                }
            }
        }
        
        result = ok(jsonResults);
        
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
