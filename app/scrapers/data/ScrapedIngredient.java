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

/**
 * Class for holding data of scraped ingredients.
 *
 * @author Oliver Dozsa
 */
public class ScrapedIngredient
{
    /* --------------------------------------------------------------------- */
    /* ATTRIBUTES                                                            */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC ATTRIBUTES ------------------------------------------------ */



    /* -- PROTECTED ATTRIBUTES --------------------------------------------- */



    /* -- PRIVATE ATTRIBUTES ----------------------------------------------- */

    /**
     * The name.
     * */
    private String name;

    /**
     * The measure string.
     * */
    private String measure;

    /**
     * The amount.
     * */
    private Double amount;



    /* --------------------------------------------------------------------- */
    /* METHODS                                                               */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC METHODS --------------------------------------------------- */

    /**
     * Initializes the scraped ingredient.
     *
     * @param name       The name.
     * @param measure    The measure string.
     * @param amount     The amount.
     * */
    public ScrapedIngredient(String name, String measure, Double amount)
    {
        this.name    = name;
        this.measure = measure;
        this.amount  = amount;
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
     * @return The amount.
     */
    public Double getAmount()
    {
        return amount;
    }

    /**
     *
     * @return The measure string.
     */
    public String getMeasure()
    {
        return measure;
    }

    /**
     *
     * @param name    The name.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     *
     * @param amount    The amount.
     */
    public void setAmount(Double amount)
    {
        this.amount = amount;
    }

    /**
     *
     * @param measure    The measure string.
     */
    public void setMeasure(String measure)
    {
        this.measure = measure;
    }

    @Override
    public String toString()
    {
        String result = "(" + amount + " " + measure + " " + name +")";

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
