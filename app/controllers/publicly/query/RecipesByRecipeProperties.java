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

package controllers.publicly.query;

import com.avaje.ebean.*;
import controllers.publicly.RecipeBrowser;
import models.Recipe;
import play.Logger;

import java.util.List;
import java.util.Map;

/**
 * Used for storing the queries for recipes by ingredients.
 *
 * @author Oliver Dozsa
 */
public class RecipesByRecipeProperties
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
     * Search by recipe properties.
     *
     * @param name                                The name fragment.
     * @param includedRecipeTags                  The included recipe tags. The key is the group id.
     * @param excludedRecipeTags                  The excluded recipe tags.
     * @param includedIngredientTags              The included ingredient tags. The key is the group id.
     * @param excludedIngredientTags              The excluded ingredient tags.
     * @param includedRecipeTagsSearchMode        Search mode for included recipe tags.
     * @param includedIngredientTagsSearchMode    Search mode for included ingredient tags.
     *
     * @return The search result page. An empty page is returned in case of an error.
     * */
    public static Query<Recipe> searchByRecipeProperties
    (
        String name,
        Map<Long, List<Long>> includedRecipeTags,
        List<Long> excludedRecipeTags,
        Map<Long, List<Long>> includedIngredientTags,
        List<Long> excludedIngredientTags,
        RecipeBrowser.SearchMode includedRecipeTagsSearchMode,
        RecipeBrowser.SearchMode includedIngredientTagsSearchMode
    )
    {
        Logger.debug(RecipesByRecipeProperties.class.getName() + ".searchByRecipeProperties(): \n" +
            "    name                             = " + name + "\n" +
            "    includedRecipeTags               = " + includedRecipeTags + "\n" +
            "    excludedRecipeTags               = " + excludedRecipeTags + "\n" +
            "    includedIngredientTags           = " + includedIngredientTags + "\n" +
            "    excludedIngredientTags           = " + excludedIngredientTags + "\n" +
            "    includedRecipeTagsSearchMode     = " + includedRecipeTagsSearchMode.name() + "\n" +
            "    includedIngredientTagsSearchMode = " + includedIngredientTagsSearchMode.name()
        );

        String rawSqlStr =
            "SELECT recipe.id FROM recipe ";

        RawSql rawSql = RawSqlBuilder.parse(rawSqlStr)
            .columnMapping("recipe.id", "id")
            .create();

        /* Prepare the query. */
        ExpressionList<Recipe> query = Recipe.find
            .setRawSql(rawSql)
            .where()
                .add(Expr.ilike("name", "%" + name + "%"));

        Query<Recipe> inclRecTagsQuery = getRecipes_RECIPE_TAGS
        (
            includedRecipeTagsSearchMode,
            includedRecipeTags,
            excludedRecipeTags
        );

        Query<Recipe> inclIngTagsQuery = getRecipes_INGREDIENT_TAGS
        (
            includedIngredientTagsSearchMode,
            includedIngredientTags,
            excludedIngredientTags
        );

        Query<Recipe> result = query
                .add(Expr.in("recipe.id", inclRecTagsQuery))
                .add(Expr.in("recipe.id", inclIngTagsQuery))
            .query();

        if(result == null)
        {
            Logger.debug(RecipesByRecipeProperties.class.getName() + ".searchByRecipeProperties(): Something went wrong!\n" );

            /* Create empty page to fulfill return criteria. */
            result = Recipe.find
                .where()
                    .eq("id", "-1")
                .query();
        }

        return result;
    }



    /* -- PROTECTED METHODS ------------------------------------------------ */



    /* -- PRIVATE METHODS -------------------------------------------------- */

    /**
     * Gets the recipes with included recipe tags' query.
     *
     * @param searchMode    The searchMode.
     *
     * @return The query.
     * */
    private static Query<Recipe> getRecipes_RECIPE_TAGS
    (
        RecipeBrowser.SearchMode searchMode,
        Map<Long, List<Long>> included,
        List<Long> excluded
    )
    {
        Logger.debug(RecipesByRecipeProperties.class.getName() + ".searchByRecipeProperties(): \n" +
            "   searchMode = " + searchMode.name() + "\n" +
            "   included   = " + included + "\n" +
            "   excluded   = " + excluded
        );

        Query<Recipe> result = null;

        switch(searchMode)
        {
            case AT_LEAST:
            {
                result = getRecipes_RECIPE_TAGS_AT_LEAST(included);

                break;
            }

            case ANY_OF:
            {
                result = getRecipes_RECIPE_TAGS_ANY_OF(included);

                break;
            }

            case GROUP:
            {
                result = getRecipes_RECIPE_TAGS_GROUP(included);

                break;
            }

            default:
            {
                Logger.warn(RecipesByRecipeProperties.class.getName() + ".getRecipes_RECIPE_TAGS(): Not supported search mode!");
            }
        }

        if(result != null)
        {
            Query<Recipe> exclSubQuery = Recipe.find
                    .select("id")
                    .where()
                        .in("tags.id", excluded)
                    .query();

            result.
                where()
                .add(Expr.not(Expr.in("recipe.id", exclSubQuery)));
        }

        return result;
    }

    /**
     * Gets the recipes with included recipe tags' query (search mode = AT_LEAST).
     *
     * @return The query.
     * */
    private static Query<Recipe> getRecipes_RECIPE_TAGS_AT_LEAST(Map<Long, List<Long>> included)
    {
        Logger.debug(RecipesByRecipeProperties.class.getName() + ".getRecipes_RECIPE_TAGS_AT_LEAST()");

        Query<Recipe> result = null;

        String rawSqlStr =
            "SELECT recipe.id FROM recipe ";

        if(included.get(Recipe.GROUP_ID_A).size() > 0)
        {
            /* Normal processing. */
            rawSqlStr +=
                "JOIN recipe_tag_recipe ON recipe.id = recipe_tag_recipe.recipe_id " +
                "GROUP BY recipe.id " +
                "HAVING COUNT(recipe_tag_recipe.recipe_tag_id) = " + included.get(Recipe.GROUP_ID_A).size();
        }
        else
        {
            /*
             * There are no included tags given, which means the user does not care about
             * tags. This means the query should select all recipes.
             * Nothing to do, since rawsqlStr selects everything by default.
             */
        }

        RawSql rawSql = RawSqlBuilder.parse(rawSqlStr)
            .columnMapping("recipe.id", "id")
            .create();

        result = Recipe.find.setRawSql(rawSql);

        if(included.get(Recipe.GROUP_ID_A).size() > 0)
        {

            result = result.where()
                .add
                (
                    Expr.in("recipe_tag_recipe.recipe_tag_id", included.get(Recipe.GROUP_ID_A))
                )
            .query();
        }
        else
        {
            /* Nothing to do. */
        }

        return result;
    }

    /**
     * Gets the recipes with included recipe tags' query (search mode = ANY_OF).
     *
     * @return The query.
     * */
    private static Query<Recipe> getRecipes_RECIPE_TAGS_ANY_OF(Map<Long, List<Long>> included)
    {
        Logger.debug(RecipesByRecipeProperties.class.getName() + ".getRecipes_RECIPE_TAGS_ANY_OF()");

        Query<Recipe> result = null;

        String rawSqlStr =
                "SELECT recipe.id FROM recipe ";

        if(included.get(Recipe.GROUP_ID_A).size() >= 0)
        {
            rawSqlStr +=
                "JOIN recipe_tag_recipe ON recipe.id = recipe_tag_recipe.recipe_id " +
                "GROUP BY recipe.id ";
        }
        else
        {
            /*
             * There are no included tags given, which means the user does not care about
             * tags. This means the query should select all recipes.
             * Nothing to do, since rawsqlStr selects everything by default.
             */
        }

        RawSql rawSql = RawSqlBuilder.parse(rawSqlStr)
            .columnMapping("recipe.id", "id")
            .create();

        result = Recipe.find.setRawSql(rawSql);

        if(included.get(Recipe.GROUP_ID_A).size() >= 0)
        {
            result = result
                .where()
                    .add
                    (
                        Expr.in("recipe_tag_recipe.recipe_tag_id", included.get(Recipe.GROUP_ID_A))
                    )
                .query();
        }
        else
        {
            /* Nothing to do. */
        }

        return result;
    }

    /**
     * Gets the recipes with included recipe tags' query (search mode = GROUP).
     *
     * @return The query.
     * */
    private static Query<Recipe> getRecipes_RECIPE_TAGS_GROUP(Map<Long, List<Long>> included)
    {
        Logger.debug(RecipesByRecipeProperties.class.getName() + ".getRecipes_RECIPE_TAGS_GROUP()");

        Query<Recipe> result = null;

        String rawSqlStr =
            "SELECT recipe.id FROM recipe ";

        boolean isNoTagsGiven = true;

        for(Long groupdId: included.keySet())
        {
            if(included.get(groupdId).size() > 0)
            {
                isNoTagsGiven = false;

                break;
            }
        }

        if(!isNoTagsGiven)
        {
            rawSqlStr +=
                "SELECT recipe.id FROM recipe " +
                "GROUP BY recipe.id ";
        }
        else
        {
            /*
             * There are no included tags given, which means the user does not care about
             * tags. This means the query should select all recipes.
             * Nothing to do, since rawsqlStr selects everything by default.
             */
        }

        RawSql rawSql = RawSqlBuilder.parse(rawSqlStr)
            .columnMapping("recipe.id", "id")
            .create();

        if(!isNoTagsGiven)
        {
            /* Normal processing. */

            ExpressionList<Recipe> disjunction = Recipe.find
                .setRawSql(rawSql)
                .where()
                .disjunction();

            for(Long groupId: included.keySet())
            {
                List<Long> groupTags = included.get(groupId);

                /* Subquery for getting recipes which tags are in the current group (AT LEAST) */
                String rawSubSqlStr =
                    "SELECT recipe.id FROM recipe " +
                    "JOIN recipe_tag_recipe ON recipe.id = recipe_tag_recipe.recipe_id " +
                    "GROUP BY recipe.id " +
                    "HAVING COUNT(recipe_tag_recipe.recipe_tag_id) = " + groupTags.size();

                RawSql rawSubSql = RawSqlBuilder.parse(rawSubSqlStr)
                        .columnMapping("recipe.id", "id")
                        .create();

                Query<Recipe> queryIncluded = Recipe.find
                        .setRawSql(rawSubSql)
                        .where()
                            .add
                            (
                                Expr.in("recipe_tag_recipe.recipe_tag_id", groupTags)
                            )
                        .query();

                /* Get recipes in main query, that have ingredients in the current group. */
                disjunction.add(Expr.in("recipe.id", queryIncluded));
            }

            result = disjunction.query();
        }
        else
        {
            result = Recipe.find.setRawSql(rawSql);
        }

        return result;
    }

    /**
     * Gets the recipes with included ingredient tags' query.
     *
     * @param searchMode    The searchMode.
     *
     * @return The query.
     * */
    private static Query<Recipe> getRecipes_INGREDIENT_TAGS
    (
        RecipeBrowser.SearchMode searchMode,
        Map<Long, List<Long>> included,
        List<Long> excluded
    )
    {
        Logger.debug(RecipesByRecipeProperties.class.getName() + ".getRecipes_INGREDIENT_TAGS()");

        Query<Recipe> result = null;

        switch(searchMode)
        {
            case AT_LEAST:
            {
                result = getRecipes_INGREDIENT_TAGS_AT_LEAST(included);
                break;
            }

            case ANY_OF:
            {
                result = getRecipes_INGREDIENT_TAGS_ANY_OF(included);

                break;
            }

            case GROUP:
            {
                result = getRecipes_INGREDIENT_TAGS_GROUP(included);

                break;
            }

            default:
            {
                Logger.warn(RecipesByRecipeProperties.class.getName() + ".getRecipes_INGREDIENT_TAGS(): Not supported search mode!");

                break;
            }
        }

        if(result != null)
        {
            Query<Recipe> exclSubQuery = Recipe.find
                .select("id")
                    .where()
                    .in("ingredients.ingredient.tags.id", excluded)
                .query();

            result.
                where()
                    .add(Expr.not(Expr.in("id", exclSubQuery)));
        }

        return result;
    }

    /**
     * Gets the recipes with included ingredient tags' query (search mode = AT_LEAST).
     *
     * @return The query.
     * */
    private static Query<Recipe> getRecipes_INGREDIENT_TAGS_AT_LEAST(Map<Long, List<Long>> included)
    {
        Logger.debug(RecipesByRecipeProperties.class.getName() + ".getRecipes_INGREDIENT_TAGS_AT_LEAST()");

        Query<Recipe> result = null;

        String rawSqlStr =
            "SELECT recipe.id FROM recipe ";

        if(included.get(Recipe.GROUP_ID_A).size() > 0)
        {
            rawSqlStr +=
                "JOIN recipe_ingredient ON recipe.id = recipe_ingredient.recipe_id " +
                "JOIN ingredient_tag_ingredient ON recipe_ingredient.ingredient_id = ingredient_tag_ingredient.ingredient_id " +
                "GROUP BY recipe.id " +
                "HAVING COUNT(ingredient_tag_ingredient.ingredient_tag_id) = " + included.get(Recipe.GROUP_ID_A).size();
        }
        else
        {
            /*
             * There are no included tags given, which means the user does not care about
             * tags. This means the query should select all recipes.
             * Nothing to do, since rawsqlStr selects everything by default.
             */
            Logger.debug(RecipesByRecipeProperties.class.getName() + ".getRecipes_INGREDIENT_TAGS_AT_LEAST(): no tags present.");
        }

        RawSql rawSql = RawSqlBuilder.parse(rawSqlStr)
            .columnMapping("recipe.id", "id")
            .create();

        result = Recipe.find.setRawSql(rawSql);

        if(included.get(Recipe.GROUP_ID_A).size() > 0)
        {
            /* Normal processing. */

            result = result
                .where()
                    .add
                    (
                        Expr.in("ingredient_tag_ingredient.ingredient_tag_id", included.get(Recipe.GROUP_ID_A))
                    )
                .query();
        }
        else
        {
            /* Nothing to do. */
        }

        return result;
    }

    /**
     * Gets the recipes with included ingredient tags' query (search mode = ANY_OF).
     *
     * @return The query.
     * */
    private static Query<Recipe> getRecipes_INGREDIENT_TAGS_ANY_OF(Map<Long, List<Long>> included)
    {
        Logger.debug(RecipesByRecipeProperties.class.getName() + ".getRecipes_INGREDIENT_TAGS_ANY_OF()");

        Query<Recipe> result = null;

        String rawSqlStr =
            "SELECT recipe.id FROM recipe ";

        if(included.get(Recipe.GROUP_ID_A).size() > 0)
        {
            rawSqlStr +=
                "JOIN recipe_ingredient ON recipe.id = recipe_ingredient.recipe_id " +
                "JOIN ingredient_tag_ingredient ON recipe_ingredient.ingredient_id = ingredient_tag_ingredient.ingredient_id " +
                "GROUP BY recipe.id ";
        }
        else
        {
            /*
             * There are no included tags given, which means the user does not care about
             * tags. This means the query should select all recipes.
             * Nothing to do, since rawsqlStr selects everything by default.
             */
            Logger.debug(RecipesByRecipeProperties.class.getName() + ".getRecipes_INGREDIENT_TAGS_ANY_OF(): no tags present.");
        }

        RawSql rawSql = RawSqlBuilder.parse(rawSqlStr)
                .columnMapping("recipe.id", "id")
                .create();

        result = Recipe.find.setRawSql(rawSql);

        if(included.get(Recipe.GROUP_ID_A).size() > 0)
        {
            /* Normal processing. */

            result = result
                .where()
                .add
                (
                    Expr.in("ingredient_tag_ingredient.ingredient_tag_id", included.get(Recipe.GROUP_ID_A))
                )
                .query();
        }
        else
        {
            /* Nothing to do. */
        }

        return result;
    }

    /**
     * Gets the recipes with included ingredient tags' query (search mode = GROUP).
     *
     * @return The query.
     * */
    private static Query<Recipe> getRecipes_INGREDIENT_TAGS_GROUP(Map<Long, List<Long>> included)
    {
        Logger.debug(RecipesByRecipeProperties.class.getName() + ".getRecipes_INGREDIENT_TAGS_GROUP()");

        Query<Recipe> result = null;

        boolean isNoTagsExist = true;

        for(Long groupdID: included.keySet())
        {
            if(included.get(groupdID).size() > 0)
            {
                isNoTagsExist = false;

                break;
            }
        }

        String rawSqlStr =
            "SELECT recipe.id FROM recipe ";

        if(!isNoTagsExist)
        {
            rawSqlStr +=
                "GROUP BY recipe.id ";
        }
        else
        {
            /*
             * There are no included tags given, which means the user does not care about
             * tags. This means the query should select all recipes.
             * Nothing to do, since rawsqlStr selects everything by default.
             */
            Logger.debug(RecipesByRecipeProperties.class.getName() + ".getRecipes_INGREDIENT_TAGS_GROUP(): no tags present.");
        }

        RawSql rawSql = RawSqlBuilder.parse(rawSqlStr)
            .columnMapping("recipe.id", "id")
            .create();

        if(!isNoTagsExist)
        {
            /* Normal processing. */

            ExpressionList<Recipe> disjunction = Recipe.find
                    .setRawSql(rawSql)
                    .where()
                    .disjunction();

            for(Long groupId: included.keySet())
            {
                List<Long> groupTags = included.get(groupId);

                /* Subquery for getting recipes with ingredients whose ingredient tags are in the current group (AT LEAST) */
                String rawSubSqlStr =
                    "SELECT recipe.id FROM recipe " +
                        "JOIN recipe_ingredient ON recipe.id = recipe_ingredient.recipe_id " +
                        "JOIN ingredient_tag_ingredient ON recipe_ingredient.ingredient_id = ingredient_tag_ingredient.ingredient_id " +
                        "GROUP BY recipe.id " +
                        "HAVING COUNT(ingredient_tag_ingredient.ingredient_tag_id) = " + groupTags.size();

                RawSql rawSubSql = RawSqlBuilder.parse(rawSubSqlStr)
                    .columnMapping("recipe.id", "id")
                    .create();

                Query<Recipe> queryIncluded = Recipe.find
                    .setRawSql(rawSubSql)
                    .where()
                        .add
                        (
                            Expr.in("ingredient_tag_ingredient.ingredient_tag_id", groupTags)
                        )
                    .query();

                /* Get recipes in main query, that have ingredients in the current group. */
                disjunction.add(Expr.in("recipe.id", queryIncluded));
            }

            result = disjunction.query();
        }
        else
        {
            result = Recipe.find.setRawSql(rawSql);
        }

        return result;
    }

    /**
     * Determines the minimum value taking into account current minimum / maximum values.
     *
     * @param actualMin    The actual minimum.
     * @param actualMax    The actual maximum.
     * @param limitMax     The limit for the maximum value.
     *
     * @return The valid minimum value, or 0, if minimum would be invalid.
     * */
    private static Integer determineMin(Integer actualMin, Integer actualMax, Integer limitMax)
    {
        Logger.debug(RecipesByRecipeProperties.class.getName() + ".determineMin()\n" +
            "   actualMin = " + actualMin + "\n" +
            "   actualMax = " + actualMax + "\n" +
            "   limitMax  = " + limitMax
        );

        Integer result = 0;

        if
        (
            (actualMax <= limitMax) &&
            (actualMin < actualMax) &&
            (actualMin >         0)
        )
        {
            /* Value is ok. */
            result = actualMin;
        }

        Logger.debug(RecipesByRecipeProperties.class.getName() + ".determineMin()\n" +
            "   result = " + result
        );

        return result;
    }

    /**
     * Determines the maximum value taking into account current minimum / maximum values.
     *
     * @param actualMin    The actual minimum.
     * @param actualMax    The actual maximum.
     * @param limitMax     The limit for the maximum value.
     *
     * @return The valid maximum value, or 0, if minimum would be invalid.
     * */
    private static Integer determineMax(Integer actualMin, Integer actualMax, Integer limitMax)
    {
        Logger.debug(RecipesByRecipeProperties.class.getName() + ".determineMax()\n" +
            "   actualMin = " + actualMin + "\n" +
            "   actualMax = " + actualMax + "\n" +
            "   limitMax  = " + limitMax
        );

        /* +1 to enforce the ignoring of max value. */
        Integer result = limitMax + 1;

        if(actualMax > actualMin)
        {
            /* Value is ok. */
            result = actualMax;
        }

        Logger.debug(RecipesByRecipeProperties.class.getName() + ".determineMax()\n" +
            "   result = " + result
        );

        return result;
    }



    /* --------------------------------------------------------------------- */
    /* OTHERS                                                                */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC OTHERS ---------------------------------------------------- */



    /* -- PROTECTED OTHERS ------------------------------------------------- */



    /* -- PRIVATE OTHERS --------------------------------------------------- */
}
