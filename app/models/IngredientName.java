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
import javax.persistence.ManyToOne;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Model;

import play.Logger;

/**
 * Model of ingredient names.
 *
 * @author Oliver Dozsa
 */
@Entity
public class IngredientName extends Model
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
     * The name.
     * */
    public String name;
    
    /**
     * The ingredient of the ingredient name.
     * */
    @ManyToOne
    public Ingredient ingredient;
    
    /**
     * The language of the ingredient name.
     * */
    @ManyToOne
    public Language language;
    
    /**
     * Finder.
     */
    public static Finder<Long, IngredientName> find = new Finder<Long, IngredientName>(IngredientName.class);



    /* -- PROTECTED ATTRIBUTES --------------------------------------------- */



    /* -- PRIVATE ATTRIBUTES ----------------------------------------------- */



    /* --------------------------------------------------------------------- */
    /* METHODS                                                               */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC METHODS --------------------------------------------------- */
    
    /**
     * Gets ingredient names with name like the given query string in the given language.
     * 
     * @param query         The query string.
     * @param languageID    The id of the language.
     * 
     * @return List of ingredient names as described above, or an empty list if none found.
     * */
    public static List<IngredientName> getNamesLikeByLanguage(String query, Long languageID)
    {
        Logger.debug(IngredientName.class.getName() + ".getNamesLikeByLanguage()\n" +
            "    query      = " + query + "\n" +
            "    languageID = " + languageID
        );

        List<IngredientName> result = null;

        if(query != null && languageID != null)
        {
            result = find
                .fetch("ingredient")
                .where()
                    .ilike("name", "%" + query + "%")
                    .eq("language_id", languageID)
                .findList();
            
            /* Check for aliases if result is empty. */
            if(result == null || result.size() == 0)
            {
                result = getNamesByAliasesLikeByLanguage(query, languageID);
            }
        }
        else
        {
            /* Log the erroneous parameter. */
            if(query == null)
            {
                Logger.error(IngredientName.class.getName() + ".getNamesLikeByLanguage(): query is null!");
            }

            if(languageID == null)
            {
                Logger.error(IngredientName.class.getName() + ".getNamesLikeByLanguage(): languageID is null!");
            }
        }
        
        if(result == null)
        {
            /* Create empty list to fulfill return criteria. */
            result = new ArrayList<IngredientName>();
        }
        
        return result;
    }
    
    /**
     * Gets an ingredient name by it's language.
     *
     * @param ingredientID    The id of the ingredient.
     * @param languageID      The language ID.
     *
     * @return The above mentioned ingredient name, or null, if none found.
     * */
    public static IngredientName getNameByLanguage(Long ingredientID, Long languageID)
    {
        Logger.debug(IngredientName.class.getName() + ".getNameByLanguage()\n" +
            "    ingredientID = " + ingredientID + "\n" +
            "    languageID   = " + languageID
        );

        IngredientName result = null;

        if(ingredientID != null && languageID   != null)
        {
            result = find.where().add(Expr.and(Expr.eq("language.id", languageID), Expr.eq("ingredient.id", ingredientID))).findUnique();
        }
        else
        {
            /* Log the erroneous parameter. */
            if(ingredientID == null)
            {
                Logger.error(IngredientName.class.getName() + ".getNameByLanguage(): ingredientID is null!");
            }

            if(languageID == null)
            {
                Logger.error(IngredientName.class.getName() + ".getNameByLanguage(): languageID is null!");
            }
        }
        
        return result;
    }



    /* -- PROTECTED METHODS ------------------------------------------------ */



    /* -- PRIVATE METHODS -------------------------------------------------- */
    
    /**
     * Gets ingredient names by aliases with name like the given query string in the given language.
     * 
     * @param query         The query string.
     * @param languageID    The id of the language.
     * 
     * @return List of ingredient names by aliases as described above, or an empty list if none found.
     * */
    private static List<IngredientName> getNamesByAliasesLikeByLanguage(String query, Long languageID)
    {
        List<IngredientName> result = new ArrayList<IngredientName>();
        
        List<IngredientAlias> aliases = IngredientAlias.find
            .fetch("ingredient")
            .where()
                .ilike("name", "%" + query + "%")
                .eq("language_id", languageID)
            .findList();
        
        if(aliases != null)
        {   
            for(IngredientAlias alias: aliases)
            {
                for(IngredientName ingName: alias.ingredient.names)
                {
                    if(ingName.language.id == languageID)
                    {
                        result.add(ingName);
                    }
                }
            }
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
