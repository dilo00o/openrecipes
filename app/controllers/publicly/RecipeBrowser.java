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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.avaje.ebean.Query;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;

import controllers.publicly.query.RecipesByIngredients;
import controllers.publicly.query.RecipesByRecipeProperties;
import models.Ingredient;
import models.IngredientName;
import models.IngredientTag;
import models.Recipe;
import models.RecipeTag;
import play.Logger;
import play.data.DynamicForm;
import play.data.FormFactory;
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
    
    /**
     * The number of recipes shown in one result page.
     * */
    public static final int RESULT_PAGE_SIZE = 20;



    /* -- PROTECTED ATTRIBUTES --------------------------------------------- */
    
    @Inject FormFactory formFactory;



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

        DynamicForm dynamicForm = formFactory.form().bindFromRequest();

        String sortBy                        = dynamicForm.get("srb");
        String sortOrder                     = dynamicForm.get("sro");
        Integer page                         = parseInteger(dynamicForm.get("pag"));

        Map<Long, List<Long>> includedIngs   = parseIncludedIds(dynamicForm.get("ini"));
        List<Long> excludedIngs              = parseExcludedIds(dynamicForm.get("exi"));
        SearchMode searchMode                = SearchMode.fromInt(parseInteger(dynamicForm.get("srm")));

        Logger.debug(RecipeBrowser.class.getName() + ".exec_searchByIngredients():\n" +
            "    sortBy          = " + sortBy + "\n" +
            "    sortOrder       = " + sortOrder + "\n" +
            "    page            = " + page + "\n" +
            "    includedIngs    = " + includedIngs + "\n" +
            "    excludedIngs    = " + excludedIngs + "\n" +
            "    searchMode      = " + searchMode.name()
        );

        Query<Recipe> searchResult = RecipesByIngredients.searchByIngredients
        (
            includedIngs,
            excludedIngs,
            searchMode
        );

        /*
         * Remove sort order, sort by, recipe tags language, and page from form data. They're not
         * part of the search specific data, and in the scala template, they're
         * added by hand, so they would be duplicated.
         */
        dynamicForm.data().remove("srb");
        dynamicForm.data().remove("sro");
        dynamicForm.data().remove("pag");
        dynamicForm.data().remove("stl");
        
        result = RecipeViewer.searchResults
        (
            searchResult,
            page,
            sortOrder,
            sortBy,
            RESULT_PAGE_SIZE,
            SearchType.BY_INGREDIENTS,
            dynamicForm.data()
        );

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
     * Executes the search by recipe properties.
     *
     * @return The result page (found recipes).
     * */
    public Result exec_searchByRecipeProperties()
    {
        Logger.debug(RecipeBrowser.class.getName() + ".exec_searchByRecipeProperties()");

        /* Retrieving data from form. */
        DynamicForm dynamicForm = formFactory.form().bindFromRequest();

        String name                              = dynamicForm.get("nam");
        String sortBy                            = dynamicForm.get("srb");
        String sortOrder                         = dynamicForm.get("sro");
        Integer page                             = parseInteger(dynamicForm.get("pag"));

        Map<Long, List<Long>> includedRecipeTags = parseIncludedIds(dynamicForm.get("inr"));
        List<Long> excludedRecipeTags            = parseExcludedIds(dynamicForm.get("exr"));
        SearchMode includedRecipeTagsSearchMode  = SearchMode.fromInt(parseInteger(dynamicForm.get("irm")));

        Map<Long, List<Long>> includedIngTags    = parseIncludedIds(dynamicForm.get("ini"));
        List<Long> excludedIngTags               = parseExcludedIds(dynamicForm.get("exi"));
        SearchMode excludedIngTagsSearchMode     = SearchMode.fromInt(parseInteger(dynamicForm.get("iim")));

        Logger.debug(RecipeBrowser.class.getName() + ".exec_searchByRecipeProperties():\n" +
            "    sortBy                       = " + sortBy + "\n" +
            "    sortOrder                    = " + sortOrder + "\n" +
            "    page                         = " + page + "\n" +
            "    includedRecipeTags           = " + includedRecipeTags + "\n" +
            "    excludedRecipeTags           = " + excludedRecipeTags + "\n" +
            "    includedRecipeTagsSearchMode = " + includedRecipeTagsSearchMode.name() + "\n" +
            "    includedIngTags              = " + includedIngTags + "\n" +
            "    excludedIngTags              = " + excludedIngTags + "\n" +
            "    excludedIngTagsSearchMode    = " + excludedIngTagsSearchMode.name() + "\n"
        );

        Query<Recipe> searchResult = RecipesByRecipeProperties.searchByRecipeProperties
        (
            name,
            includedRecipeTags,
            excludedRecipeTags,
            includedIngTags,
            excludedIngTags,
            includedRecipeTagsSearchMode,
            excludedIngTagsSearchMode
        );

        /*
         * Remove sort order, sort by, tag language, and page from form data. They're not
         * part of the search specific data, and in the scala template, they're
         * added by hand, so they would be duplicated.
         */
        dynamicForm.data().remove("srb");
        dynamicForm.data().remove("sro");
        dynamicForm.data().remove("pag");
        dynamicForm.data().remove("stl");

        return RecipeViewer.searchResults
        (
            searchResult,
            page,
            sortOrder,
            sortBy,
            RESULT_PAGE_SIZE,
            SearchType.BY_RECIPE_PROPERTIES,
            dynamicForm.data()
        );
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
    
    /**
     * Gets recipe tags based on the query string (used in AJAX queries).
     * 
     * @param query             The query string.
     * 
     * @return The available recipe tags as a json object, according to typeahead format.
     * */
    public Result recipeTags(String query)
    {
        Logger.debug(RecipeBrowser.class.getName() + ".recipeTags():\n" +
            "    query = " + query
        );

        Result result = null;
        
        ArrayNode jsonResults = Json.newObject().arrayNode();
        
        if(query.length() > 0)
        {
            List<RecipeTag> tags = RecipeTag.getTagsByNameLike(query);
            
            for(RecipeTag tag: tags)
            {
                ObjectNode queryJsonResult = Json.newObject();

                /* We assume, that name is not null (db restriction) */
                queryJsonResult.put("value", tag.name);
                queryJsonResult.put("id", tag.id);
                
                ArrayNode tokens = queryJsonResult.putArray("tokens");
                tokens.add(tag.name);
                
                jsonResults.add(queryJsonResult);
            }
        }
        
        result = ok(jsonResults);
        
        return result;
    }



    /* -- PROTECTED METHODS ------------------------------------------------ */



    /* -- PRIVATE METHODS -------------------------------------------------- */
    
    /**
     * Parses the included ingredient ids from its string representation.
     * The string representation format is:
     *     [group_id_n1]:[group_id_n2]:...:[id_n],...
     *
     * @param includedStr    The string representation.
     *
     * @return A map in which the key is the group id, and the element are list containing
     * ingredient ids belonging to the group. A map with "group A" only containing an empty
     * list is returned in case parsing fails.
     * */
    private static Map<Long, List<Long>> parseIncludedIds(String includedStr)
    {
        Logger.debug(RecipeBrowser.class.getName() + ".parseIncludedIds(): includedStr = " + includedStr);

        Map<Long, List<Long>> result = new HashMap<Long, List<Long>>();

        /* Get the group-id tokens. */
        if(includedStr != null)
        {
            StringTokenizer tokenizer = new StringTokenizer(includedStr, ",");

            while(tokenizer.hasMoreTokens())
            {
                String token = tokenizer.nextToken();

                /* Get groups, and id. */
                StringTokenizer groupTokenizer = new StringTokenizer(token, ":");

                List<String> groupsId = new ArrayList<String>();

                while(groupTokenizer.hasMoreTokens())
                {
                    groupsId.add(groupTokenizer.nextToken());
                }

                if(groupsId.size() > 1)
                {
                    /* The id in question is the last element. */
                    String idStr = groupsId.get(groupsId.size() - 1);
                    Long id      = parseLongNum(idStr);

                    groupsId.remove(groupsId.size() - 1);

                    /* Getting group id-s. */
                    for(String groupStr: groupsId)
                    {
                        Long groupId = groupToId(groupStr);

                        if(groupId != Recipe.GROUP_ID_0)
                        {
                            /* Group is valid, let's add the id to the result */

                            if(result.get(groupId) == null)
                            {
                                /* Create new list for the group. */
                                result.put(groupId, new ArrayList<Long>());
                            }

                            result.get(groupId).add(id);
                        }
                        else
                        {
                            /* Group id is invalid. */
                            Logger.warn(RecipeBrowser.class.getName() + ".parseIncludedIds(): groupId is invalid!");
                        }
                    }
                }
                else
                {
                    Logger.warn(RecipeBrowser.class.getName() + ".parseIncludedIds(): groupsId.size() < 1!\n" +
                        "    groupsId = " + groupsId
                    );
                }
            }
        }
        else
        {
            Logger.warn(RecipeBrowser.class.getName() + ".parseIncludedIds(): includedStr is null!");
        }

        /* If parsing the ids failed, create an empty result. */
        if(result.size() == 0)
        {
            Logger.warn(RecipeBrowser.class.getName() + ".parseIncludedIds(): failed to parse!");

            result.put(Recipe.GROUP_ID_A, new ArrayList<Long>());
        }

        return result;
    }

    /**
     * Parses the excluded ingredient ids from its string representation.
     *
     * @param excludedStr    The string representation.
     *
     * @return The list of excluded ingredient ids.
     * */
    private static List<Long> parseExcludedIds(String excludedStr)
    {
        Logger.debug(RecipeBrowser.class.getName() + ".parseExcludedIds(): excludedStr = " + excludedStr);

        List<Long> result = new ArrayList<Long>();

        if(excludedStr != null)
        {
            /* Get the id tokens. */
            StringTokenizer tokenizer = new StringTokenizer(excludedStr, ",");

            while(tokenizer.hasMoreTokens())
            {
                String token = tokenizer.nextToken();

                Long id = parseLongNum(token);

                result.add(id);
            }
        }
        else
        {
            Logger.warn(RecipeBrowser.class.getName() + ".parseExcludedIds(): excludedStr is null!");
        }

        return result;
    }

    /**
     * Converts a group string to a group id.
     *
     * @param groupStr    The group string.
     *
     * @return The group id.
     * */
    private static Long groupToId(String groupStr)
    {
        Logger.debug(RecipeBrowser.class.getName() + ".groupToId(): groupStr = " + groupStr);

        Long result = Recipe.GROUP_ID_0;

        if(groupStr.equalsIgnoreCase("A"))
        {
            result = Recipe.GROUP_ID_A;
        }

        if(groupStr.equalsIgnoreCase("B"))
        {
            result = Recipe.GROUP_ID_B;
        }

        if(groupStr.equalsIgnoreCase("C"))
        {
            result = Recipe.GROUP_ID_C;
        }

        if(groupStr.equalsIgnoreCase("D"))
        {
            result = Recipe.GROUP_ID_D;
        }

        if(groupStr.equalsIgnoreCase("E"))
        {
            result = Recipe.GROUP_ID_E;
        }

        if(groupStr.equalsIgnoreCase("F"))
        {
            result = Recipe.GROUP_ID_F;
        }

        return result;
    }

    /**
     * Converts a string to Integer. If conversion fails,
     * returns 0.
     *
     * @return The converted integer.
     * */
    private static Integer parseInteger(String intStr)
    {
        Logger.debug(RecipeBrowser.class.getName() + ".parseInteger(): intStr = " + intStr);

        Integer result = 0;

        try
        {
            result = Integer.parseInt(intStr);
        }
        catch(NumberFormatException e)
        {
            Logger.warn(RecipeBrowser.class.getName() + ".parseInteger():\n" +
                "    NumberFormatException! intStr = " + intStr);
        }
        catch(Exception e)
        {
            Logger.warn(RecipeBrowser.class.getName() + ".parseInteger():\n" +
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
        Logger.debug(RecipeBrowser.class.getName() + ".parseLongNum(): longStr = " + longStr);

        Long result = 0L;

        try
        {
            result = Long.parseLong(longStr);
        }
        catch(NumberFormatException e)
        {
            Logger.warn(RecipeBrowser.class.getName() + ".parseLongNum():\n" +
                "    NumberFormatException! longStr = " + longStr);
        }
        catch(Exception e)
        {
            Logger.warn(RecipeBrowser.class.getName() + ".parseLongNum():\n" +
                "    Exception! longStr = " + longStr);
        }

        return result;
    }



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
