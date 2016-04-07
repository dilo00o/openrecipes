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
 * Created by Oliver Dozsa on 2014.06.29.
 *
 * @author Oliver Dozsa
 */
public class RecipesByIngredients
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
     * Gets recipes based on the given ingredient constraints.
     *
     * @param includedIngredientIds Basically this parameter contains the ingredient to include in the search.
     *                              This paramater's interpretation depends on the search mode.
     *                              If the search mode is not GROUP, then only the first element is considered (which
     *                              is a list of ingredient ids).
     *                              If the search mode is group, all elements are considered.
     * @param excludedIngredientIds Ingredient that should be excluded. Note, that an ingredient can either be included,
     *                              or excluded, but not both at the same time.
     * @param searchMode            The search mode.
     *                                - EXACT:    Exactly the given ingredients should be present in the recipe (AND
     *                                            relationship between ingredients, and no more, or less ingredients).
     *                                - AT_LEAST: At least the given ingredients should be present in the recipe, but
     *                                            there can be more (AND relationship between ingredient).
     *                                - ANY_OF:   Any of the given ingredient could be present in the recipe (OR
     *                                            relationship between ingredients).
     *                                - GROUP:    The user can form group of ingredients. These groups will be treated
     *                                            as one unit (AT LEAST). Between the group, there's OR relationship (ANY_OF).
     *                                            So the meaning of group mode is: Any of the given groups's all ingredients
     *                                            should be present.
     *
     * @return The list of found ingredients query based on the criteria, or an empty list,
     *         if nothing is found.
     * */
    public static Query<Recipe> searchByIngredients
    (
        Map<Long, List<Long>> includedIngredientIds,
        List<Long> excludedIngredientIds,
        RecipeBrowser.SearchMode searchMode
    )
    {
        Logger.debug(RecipesByIngredients.class.getName() + ".searchByIngredients(): \n" +
            "    includedIngredientIds = " + includedIngredientIds + "\n" +
            "    excludedIngredientIds = " + excludedIngredientIds + "\n" +
            "    searchMode            = " + searchMode.name()
        );

        Query<Recipe> result = null;

        /* Check whether included, and excluded list are mutually exclusive. */
        boolean isMutuallyExclusive = true;

        for(Long excluded: excludedIngredientIds)
        {
            for(List<Long> includedList: includedIngredientIds.values())
            {
                if(includedList.contains(excluded))
                {
                    /* Included list contains an excluded ingredient. */
                    isMutuallyExclusive = false;

                    break;
                }
            }

            if(!isMutuallyExclusive)
            {
                break;
            }
        }

        if(isMutuallyExclusive)
        {
            switch(searchMode)
            {
                case EXACT:
                {
                    /* In exact search, excluded ingredient has no meaning. */

                    result = searchByIngredients_EXACT(includedIngredientIds);

                    break;
                }

                case AT_LEAST:
                {
                    result = searchByIngredients_AT_LEAST(includedIngredientIds, excludedIngredientIds);

                    break;
                }

                case ANY_OF:
                {
                    result = searchByIngredients_ANY_OF(includedIngredientIds, excludedIngredientIds);

                    break;
                }

                case GROUP:
                {
                    result = searchByIngredients_GROUP(includedIngredientIds, excludedIngredientIds);

                    break;
                }

                case UNKNOWN:
                default:
                {
                    Logger.warn(RecipesByIngredients.class.getName() + ".searchByIngredients(): searchMode is unknown!");

                    /* Create empty list to fulfill return criteria. */
                    result = Recipe.find
                        .where()
                            .eq("id", "-1")
                        .query();

                    break;
                }
            }
        }
        else
        {
            Logger.warn
            (
                RecipesByIngredients.class.getName() + ".searchByIngredients(): included, and excluded list is not mutually exclusive!\n" +
                "    included = " + includedIngredientIds + "\n" +
                "    excluded = " + excludedIngredientIds
            );

            /* Create empty page to fulfill return criteria. */
            result = Recipe.find
                .where()
                    .eq("id", "-1")
                .query();
        }

        if(result == null)
        {
            Logger.warn(RecipesByIngredients.class.getName() + ".searchByIngredients(): something went wrong!");

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
     * Executes the EXACT search.
     *
     * @param includedIngredientIds    The included ingredient ids. The key is the group id, the values are
     *                                 the ingredient ids in for the group.
     *
     * @return The query.
     * */
    private static Query<Recipe> searchByIngredients_EXACT
    (
        Map<Long, List<Long>> includedIngredientIds
    )
    {
        Logger.debug(RecipesByIngredients.class.getName() + ".searchByIngredients_EXACT()");

        Query<Recipe> result = null;

        String rawSqlStr =
            "SELECT recipe.id FROM recipe " +
            "JOIN recipe_ingredient ON recipe.id = recipe_ingredient.recipe_id " +
            "GROUP BY recipe.id " +
            "HAVING COUNT(recipe_ingredient.ingredient_id) = " + includedIngredientIds.get(Recipe.GROUP_ID_A).size();

        RawSql rawSql = RawSqlBuilder.parse(rawSqlStr)
            .columnMapping("recipe.id", "id")
            .create();

        result = Recipe.find.setRawSql(rawSql)
            .where()
                .conjunction()
                    .add(Expr.in("recipe_ingredient.ingredient_id", includedIngredientIds.get(Recipe.GROUP_ID_A)))
                    .add(Expr.in("recipe.id", Recipe.find.setRawSql(rawSql)))
            .query();

        return result;
    }

    /**
     * Executes the AT_LEAST search.
     *
     * @param includedIngredientIds    The included ingredient ids. The key is the group id, the values are
     *                                 the ingredient ids in for the group.
     * @param excludedIngredientIds    The excluded ingredient ids.
     *
     * @return The query.
     * */
    private static Query<Recipe> searchByIngredients_AT_LEAST
    (
        Map<Long, List<Long>> includedIngredientIds,
        List<Long> excludedIngredientIds
    )
    {
        Logger.debug(RecipesByIngredients.class.getName() + ".searchByIngredients_AT_LEAST()");

        Query<Recipe> result = null;

        String rawSqlStr =
            "SELECT recipe.id FROM recipe " +
            "JOIN recipe_ingredient ON recipe.id = recipe_ingredient.recipe_id " +
            "GROUP BY recipe.id " +
            "HAVING COUNT(recipe_ingredient.ingredient_id) = " + includedIngredientIds.get(Recipe.GROUP_ID_A).size();

        RawSql rawSql = RawSqlBuilder.parse(rawSqlStr)
            .columnMapping("recipe.id", "id")
            .create();

        Query<Recipe> exclSubQuery = Recipe.find.select("id").where().in("ingredients.ingredient.id", excludedIngredientIds).query();

        result = Recipe.find.setRawSql(rawSql)
            .where()
                .conjunction()
                    .add(Expr.in("recipe_ingredient.ingredient_id", includedIngredientIds.get(Recipe.GROUP_ID_A)))
                    .add(Expr.not(Expr.in("recipe.id", exclSubQuery)))
            .query();

        return result;
    }

    /**
     * Executes the ANY_OF search.
     *
     * @param includedIngredientIds    The included ingredient ids. The key is the group id, the values are
     *                                 the ingredient ids in for the group.
     * @param excludedIngredientIds    The excluded ingredient ids.
     *
     * @return The query.
     * */
    private static Query<Recipe> searchByIngredients_ANY_OF
    (
        Map<Long, List<Long>> includedIngredientIds,
        List<Long> excludedIngredientIds
    )
    {
        Logger.debug(RecipesByIngredients.class.getName() + ".searchByIngredients_ANY_OF()");

        Query<Recipe> result = null;

        String rawSqlStr =
            "SELECT recipe.id FROM recipe " +
            "JOIN recipe_ingredient ON recipe.id = recipe_ingredient.recipe_id " +
            "GROUP BY recipe.id"; // TODO: Group by is not needed, since there's no aggregate function used

        RawSql rawSql = RawSqlBuilder.parse(rawSqlStr)
            .columnMapping("recipe.id", "id")
            .create();

        Query<Recipe> exclSubQuery = Recipe.find.select("id").where().in("ingredients.ingredient.id", excludedIngredientIds).query();

        result = Recipe.find.setRawSql(rawSql)
            .where()
                .conjunction()
                    .add(Expr.in("recipe_ingredient.ingredient_id", includedIngredientIds.get(Recipe.GROUP_ID_A)))
                    .add(Expr.not(Expr.in("recipe.id", exclSubQuery)))
            .query();

        return result;
    }

    /**
     * Executes the search GROUP mode.
     *
     * @param includedIngredientIds    Included ingredient ids. The key is the group id, the values are
     *                                 the ingredient ids for the group.
     * @param excludedIngredientIds    Excluded ingredient ids.
     * @param usedLanguage             The used language for resulting recipes' names.
     *
     * @return The query.
     * */
    private static Query<Recipe> searchByIngredients_GROUP
    (
        Map<Long, List<Long>> includedIngredientIds,
        List<Long> excludedIngredientIds
    )
    {
        Logger.debug(RecipesByIngredients.class.getName() + ".searchByIngredients_GROUP()");

        Query<Recipe> result = null;

        Logger.debug("searchByIngredients_GROUP()");

        String rawSqlStr =
            "SELECT recipe.id FROM recipe " +
            "GROUP BY recipe.id ";

        RawSql rawSql = RawSqlBuilder.parse(rawSqlStr)
            .columnMapping("recipe.id", "id")
            .create();

        ExpressionList<Recipe> disjunction = Recipe.find
            .setRawSql(rawSql)
            .where()
                .disjunction();

        for(Long groupId: includedIngredientIds.keySet())
        {
            List<Long> groupIngs = includedIngredientIds.get(groupId);

            /* Subquery for getting recipes which ingredients are in the current group (AT LEAST) */
            String rawSubSqlStr =
                "SELECT recipe.id FROM recipe " +
                "GROUP BY recipe.id " +
                "HAVING COUNT(recipe_ingredient.ingredient_id) = " + groupIngs.size();

            RawSql rawSubSql = RawSqlBuilder.parse(rawSubSqlStr)
                .columnMapping("recipe.id", "id")
                .create();

            Query<Recipe> exclSubQuery = Recipe.find.select("id").where().in("ingredients.ingredient.id", excludedIngredientIds).query();

            Query<Recipe> queryIncluded = Recipe.find
                .setRawSql(rawSubSql)
                .where()
                    .and
                    (
                        Expr.in("recipe_ingredient.ingredient_id", groupIngs),
                        Expr.not(Expr.in("recipe.id", exclSubQuery))
                    )
                .query();

            /* Get recipes in main query, that have ingredients in the current group. */
            disjunction.add(Expr.in("recipe.id", queryIncluded));
        }

        result = disjunction.query();

        return result;
    }



    /* --------------------------------------------------------------------- */
    /* OTHERS                                                                */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC OTHERS ---------------------------------------------------- */



    /* -- PROTECTED OTHERS ------------------------------------------------- */



    /* -- PRIVATE OTHERS --------------------------------------------------- */
}
