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

package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Model;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;

import play.Logger;

/**
 * Model of ingredients.
 *
 * @author Oliver Dozsa
 */
@Entity
public class Ingredient extends Model
{
    /* --------------------------------------------------------------------- */
    /* ATTRIBUTES                                                            */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC ATTRIBUTES ------------------------------------------------ */
    
    /**
     * The unique identifier.
     */
    @Id
    @GeneratedValue
    public Long id;
    
    /**
     * The ingredient names.
     * */
    @OneToMany(mappedBy = "ingredient")
    public List<IngredientName> names;
    
    /**
     * Used for the recipe - ingredients relationship.
     * */
    @OneToMany(mappedBy = "ingredient")
    public List<RecipeIngredient> recipeIngredients;
    
    /**
     * The tags of the ingredient.
     * */
    @ManyToMany(mappedBy = "ingredients")
    public List<IngredientTag> tags;
    
    /**
     * The ingredient names.
     * */
    @OneToMany(mappedBy = "ingredient")
    public List<IngredientAlias> aliases;
    
    /**
     * Finder.
     */
    public static Finder<Long, Ingredient> find = new Finder<Long, Ingredient>(Ingredient.class);



    /* -- PROTECTED ATTRIBUTES --------------------------------------------- */



    /* -- PRIVATE ATTRIBUTES ----------------------------------------------- */



    /* --------------------------------------------------------------------- */
    /* METHODS                                                               */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC METHODS --------------------------------------------------- */
    
    /**
     * Gets ingredients with names like the given query string in the given language.
     * 
     * @param query         The query string.
     * @param languageID    The query language.
     * 
     * @return A list of ingredients as described above, or an empty list is returned, if none found.
     * */
    public static List<Ingredient> getIngredientsLikeByLanguage(String query, Long languageID)
    {
        Logger.debug(Ingredient.class.getName() + ".getIngredientsLikeByLanguage():\n" +
            "    query      = " + query + "\n" +
            "    languageID = " + languageID
        );

        List<Ingredient> result = new ArrayList<Ingredient>();
        
        List<IngredientName> names = IngredientName.getNamesLikeByLanguage(query, languageID);
        
        for(IngredientName name: names)
        {
            result.add(name.ingredient);
        }
        
        return result;
    }
    
    /**
     * Gets the name with the given language.
     * 
     * @param languageID    The id of the language.
     * 
     * @return The name with the given language, or null, if no name with the given language is found.
     * */
    public IngredientName getNameByLanguage(Long languageID)
    {
        Logger.debug(Ingredient.class.getName() + ".getNameByLanguage():\n" +
            "    languageID = " + languageID
        );

        IngredientName result = null;

        if(languageID != null)
        {
            for(IngredientName name: names)
            {
                if(name.language.id == languageID)
                {
                    result = name;

                    break;
                }
            }
        }
        else
        {
            Logger.error(Ingredient.class.getName() + ".getNameByLanguage(): languageID is null!");
        }
        
        return result;
    }
    
    /**
     * Gets the ingredients with the given tag.
     * 
     * @param tag    The tag.
     * 
     * @return A list of ingredients with the given tag. An empty list is returned if no such ingredient is found.
     * */
    public static List<Ingredient> getIngredientsWithTag(IngredientTag tag)
    {
        List<Ingredient> result = null;

        if(tag != null)
        {
            Logger.debug(Ingredient.class.getName() + ".getIngredientsWithTag():\n" +
                "    tag.id = " + tag.id
            );

            result = find.fetch("names")
                .fetch("tags")
                .where()
                    .eq("tags.id", tag.id)
                .findList();
        }
        else
        {
            Logger.error(Ingredient.class.getName() + ".getIngredientsWithTag(): tag is null!");
        }
        
        if(result == null)
        {
            result = new ArrayList<Ingredient>();
        }
        
        return result;
    }
    
    /**
     * Gets ingredient with given tags, and min / max constraint.
     *
     * @param isAnd        If true, ingredient with at least containing all the given tags will be searched
     * @param minkcal      The minimum kcal value.
     * @param maxkcal      The maximum kcal value.
     *
     * @return The list of ingredient with the given constraints. An empty list is returned in case of no such
     * ingredients found.
     * */
    public static List<Ingredient> getIngredientsWithTags(List<IngredientTag> tags, boolean isAnd)
    {
        List<Ingredient> result = null;

        if(tags != null)
        {
            /* Params are valid. */
            Logger.debug(Ingredient.class.getName() + ".getIngredientsWithTags():" +
                " tags    = " + tags    + "\n" +
                " isAnd   = " + isAnd
            );

            String tagIds = "";

            int i;
            for(i = 0; i < tags.size() - 1; i++)
            {
                IngredientTag tag = tags.get(i);

                tagIds += tag.id + ", ";
            }

            IngredientTag lastTag = tags.get(i);

            tagIds += lastTag.id;

            String sql =
                " SELECT ingredient.id" +
                " FROM ingredient" +
                " JOIN ingredient_tag_ingredient on ingredient.id = ingredient_tag_ingredient.ingredient_id" +
                " WHERE" +
                "     ingredient_tag_ingredient.ingredient_tag_id IN (" + tagIds + ")" +
                " GROUP BY ingredient.id";

            if(isAnd)
            {
                sql += " HAVING COUNT(*) = " + tags.size();
            }

            RawSql rawSql = RawSqlBuilder.parse(sql).create();

            Query<Ingredient> sqlQuery = Ebean.find(Ingredient.class);

            sqlQuery.setRawSql(rawSql);

            result = sqlQuery.findList();
        }
        else
        {
            /* Log the erroneous params. */

            if(tags == null)
            {
                Logger.error(Ingredient.class.getName() + ".getIngredientsWithTags(): tags is null!");
            }
        }

        /* To fulfill return criteria. */
        if(result == null)
        {
            result = new ArrayList<Ingredient>();
        }
        
        return result;
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