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

import com.avaje.ebean.Model;

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



    /* -- PROTECTED METHODS ------------------------------------------------ */



    /* -- PRIVATE METHODS -------------------------------------------------- */



    /* --------------------------------------------------------------------- */
    /* OTHERS                                                                */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC OTHERS ---------------------------------------------------- */



    /* -- PROTECTED OTHERS ------------------------------------------------- */



    /* -- PRIVATE OTHERS --------------------------------------------------- */

}