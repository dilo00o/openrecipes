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

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import com.avaje.ebean.Model;

/**
 * Model class for recipes.
 *
 * @author Oliver Dozsa
 */
@Entity
public class Recipe extends Model
{
    /* --------------------------------------------------------------------- */
    /* ATTRIBUTES                                                            */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC ATTRIBUTES ------------------------------------------------ */
    
    /* Group ids for included ingredients. */
    public static final Long GROUP_ID_A = 1L;
    public static final Long GROUP_ID_B = 2L;
    public static final Long GROUP_ID_C = 3L;
    public static final Long GROUP_ID_D = 4L;
    public static final Long GROUP_ID_E = 5L;
    public static final Long GROUP_ID_F = 6L;

    public static final Long GROUP_ID_0 = 0L;
    
    /**
     * The maximum number of ingredients used in search.
     * */
    public static final Integer MAX_NUM_OF_INGREDIENTS = 30;
    
    /**
     * The unique id.
     * */
    @Id
    @GeneratedValue
    public Long id;
    
    /**
     * The name.
     */
    @Lob
    public String name;
    
    /**
     * URL of the recipe.
     */
    @Lob
    public String url;
    
    /**
     * The list of ingredients.
     * */
    @OneToMany(mappedBy = "recipe")
    public List<RecipeIngredient> ingredients;
    
    /**
     * The list of tags.
     * */
    @ManyToMany(mappedBy = "recipes")
    public List<RecipeTag> tags;
    
    /**
     * Finder.
     */
    public static Finder<Long, Recipe> find = new Finder<Long, Recipe>(Recipe.class);



    /* -- PROTECTED ATTRIBUTES --------------------------------------------- */



    /* -- PRIVATE ATTRIBUTES ----------------------------------------------- */



    /* --------------------------------------------------------------------- */
    /* METHODS                                                               */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC METHODS --------------------------------------------------- */



    /* -- PROTECTED METHODS ------------------------------------------------ */



    /* -- PRIVATE METHODS -------------------------------------------------- */



    /* --------------------------------------------------------------------- */
    /* OTHERS                                                                */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC OTHERS ---------------------------------------------------- */



    /* -- PROTECTED OTHERS ------------------------------------------------- */



    /* -- PRIVATE OTHERS --------------------------------------------------- */
}
