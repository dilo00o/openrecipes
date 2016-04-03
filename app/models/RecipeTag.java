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

import com.avaje.ebean.Model;
import com.avaje.ebean.Query;

import play.Logger;

/**
 * Model class for recipe tags.
 *
 * @author Oliver Dozsa
 */
@Entity
public class RecipeTag extends Model
{
    /* --------------------------------------------------------------------- */
    /* ATTRIBUTES                                                            */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC ATTRIBUTES ------------------------------------------------ */
    
    /**
     * The id.
     * */
    @Id
    @GeneratedValue
    public Long id;
    
    /**
     * The tag name.
     * */
    public String name;
    
    /**
     * Used to map the many to many relationship
     * recipes - recipe tags
     * */
    @ManyToMany
    public List<Recipe> recipes;
    
    /**
     * Finder
     * */
    public static Finder<Long, RecipeTag> find = new Finder<Long, RecipeTag>(RecipeTag.class);



    /* -- PROTECTED ATTRIBUTES --------------------------------------------- */



    /* -- PRIVATE ATTRIBUTES ----------------------------------------------- */



    /* --------------------------------------------------------------------- */
    /* METHODS                                                               */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC METHODS --------------------------------------------------- */
    
    /**
     * Get tags by name like the given name.
     *
     * @param nameLike      The name like parameter.
     * @param languageId    The language id of the tags.
     * 
     * @return A list of tags. An empty list is returned, if no such tags found.
     * */
    public static List<RecipeTag> getTagsByNameLike(String nameLike)
    {
        Logger.debug(RecipeTag.class.getName() + ".getTagsByNameLike():\n" +
            "   nameLike = " + nameLike
        );

        List<RecipeTag> result = null;

        if(nameLike != null)
        {
            Query<RecipeTag> resultQuery = find
                .where()
                    .ilike("name", "%" + nameLike + "%")
                .query();

            result = resultQuery.findList();
        }
        else
        {
            Logger.error(RecipeTag.class.getName() + ".getTagsByNameLike(): nameLike is null!");
        }
        
        if(result == null)
        {
            /* Return an empty array to fulfill return criteria. */
            result = new ArrayList<RecipeTag>();
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
