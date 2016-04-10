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

package scrapers.visitors;

import play.Logger;
import scrapers.data.ScrapedRecipe;

import java.util.Enumeration;

/**
 * The base visitor class. All visitors should descend from this class.
 * The goal of this abstract class is to support robust visiting of web pages, so
 * even in case of errors it should still be able to work.
 *
 * @author Oliver Dozsa
 */
public abstract class RecipeSiteVisitor implements Enumeration<ScrapedRecipe>
{
    /* --------------------------------------------------------------------- */
    /* ATTRIBUTES                                                            */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC ATTRIBUTES ------------------------------------------------ */



    /* -- PROTECTED ATTRIBUTES --------------------------------------------- */
    
    /**
     * The error code.
     */
    protected ErrorCode errorCode;



    /* -- PRIVATE ATTRIBUTES ----------------------------------------------- */

    /**
     * The state of the visitor.
     * */
    private VisitorState state;

    /**
     * The id of the visitor.
     */
    private int id;


    /* --------------------------------------------------------------------- */
    /* METHODS                                                               */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC METHODS --------------------------------------------------- */

    /**
     * Initializes the state to INIT.
     *
     * @param id    The unique ID of the visitor.
     * */
    public RecipeSiteVisitor(int id)
    {
        this.id   = id;
        state     = VisitorState.INIT;
        errorCode = ErrorCode.ERROR_NO_ERROR;
    }

    /**
     * Checks whether the connection is established to the page.
     *
     * @return True, if the connection is established to the page, false otherwise.
     */
    public abstract boolean isConnected();

    /**
     * This method should be used to recover from error state to try to continue from last working state.
     */
    public abstract void recover();
    
    /**
     * Skips the currently tried to scrape recipe.
     * Useful if recipe contains invalid data (e.g. no ingredients, etc...), and because of that,
     * simple recover wouldn't work.
     */
    public abstract void skipCurrentElement();
    
    /**
     * Skips the currently tried to scrape recipe.
     * Useful if recipe site failed to load.
     */
    public abstract void skipCurrentPage();

    /**
     * @return The actual state.
     */
    public VisitorState getState()
    {
        return state;
    }

    /**
     *
     * @return The id.
     */
    public int getId()
    {
        return id;
    }
    
    /**
     * @return The error code.
     */
    public ErrorCode getErrorCode()
    {
        return errorCode;
    }

    /* -- PROTECTED METHODS ------------------------------------------------ */

    /**
     * Changes to the new state.
     *
     * @param newState
     */
    protected void changeState(VisitorState newState)
    {
        Logger.debug(this.getClass().getName() + ".changeState(): " + state.name() + " -> " + newState.name());

        state = newState;
    }



    /* -- PRIVATE METHODS -------------------------------------------------- */



    /* --------------------------------------------------------------------- */
    /* OTHERS                                                                */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC OTHERS ---------------------------------------------------- */

    /**
     * The state of the visitor.
     * */
    public static enum VisitorState
    {
        /**
         * The initial state.
         */
        INIT,

        /**
         * The working state. It means the visitor is functioning correctly.
         */
        WORK,

        /**
         * Some error happened during working. In this state, the visitor should
         * always return null for calls to nextElement().
         */
        ERROR;
    }
    
    public static enum ErrorCode
    {
        /**
         * No error.
         */
        ERROR_NO_ERROR,
        
        /**
         * Error happened during initialization.
         */
        ERROR_INIT,
        
        /**
         * Failed to scrape the current element.
         */
        ERROR_SCRAPE,
        
        /**
         * Failed to load the recipe page.
         */
        ERROR_PAGE_LOAD;
    }



    /* -- PROTECTED OTHERS ------------------------------------------------- */



    /* -- PRIVATE OTHERS --------------------------------------------------- */
}
