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

package scrapers.data;

import java.util.List;

/**
 * Class for holding data of scraped recipes.
 *
 * @author Oliver Dozsa
 */
public class ScrapedRecipe
{
    /* --------------------------------------------------------------------- */
    /* ATTRIBUTES                                                            */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC ATTRIBUTES ------------------------------------------------ */



    /* -- PROTECTED ATTRIBUTES --------------------------------------------- */



    /* -- PRIVATE ATTRIBUTES ----------------------------------------------- */

    /**
     * The url of the recipe.
     * */
    private String url;

    /**
     * The ingredient list.
     * */
    private List<ScrapedIngredient> ingredientList;

    /**
     * The number of servings.
     * */
    private Integer servings;

    /**
     * The name of the recipe.
     * */
    private String name;



    /* --------------------------------------------------------------------- */
    /* METHODS                                                               */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC METHODS --------------------------------------------------- */

    /**
     * Initializes the recipe with its URL.
     *
     * @param url    The url string.
     */
    public ScrapedRecipe(String url)
    {
        this.url = url;
    }

    /**
     *
     * @return The ingredient list.
     */
    public List<ScrapedIngredient> getIngredientList()
    {
        return ingredientList;
    }

    /**
     *
     * @return The url string.
     */
    public String getUrl()
    {
        return url;
    }

    /**
     *
     * @param url    The new url string.
     */
    public void setUrl(String url)
    {
        this.url = url;
    }

    /**
     *
     * @return The number of servings.
     */
    public Integer getServings()
    {
        return servings;
    }

    /**
     *
     * @param servings    The new number of servings.
     */
    public void setServings(Integer servings)
    {
        this.servings = servings;
    }

    /**
     *
     * @return The name.
     */
    public String getName()
    {
        return name;
    }

    /**
     *
     * @param name The new name.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     *
     * @param ingredientList The new ingredient list.
     */
    public void setIngredientList(List<ScrapedIngredient> ingredientList)
    {
        this.ingredientList = ingredientList;
    }

    @Override
    public String toString()
    {
        String result = "ScrapedRecipe:\n" +
            "    url         = " + url + "\n" +
            "    name        = " + name + "\n" +
            "    servings    = " + servings + "\n" +
            "    ingredients = " + ingredientList;

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
