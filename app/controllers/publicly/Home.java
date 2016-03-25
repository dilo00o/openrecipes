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

package controllers.publicly;

import models.Language;
import play.Logger;
import play.api.mvc.Call;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.publicviews.home.*;

/**
 * Controller for home page.
 *
 * @author Oliver Dozsa
 */
public class Home extends Controller
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
     * Returns the home page.
     *
     * @return The home page.
     * */
    public Result index()
    {
        Logger.debug(Home.class.getName() + ".index()");

        Result result = null;

        result = ok
        (
            home.render()
        );

        return result;
    }
    
    /**
     * Changes the language of the whole site (does not affect search results).
     *
     * @param languageId    The target language's id.
     * @param previous      The current url. Will be used to return to after the language has changed.
     *
     * @return Returns the previous page but with the new language settings effective.
     * */
    public Result changeLanguage(Long languageId, String previous)
    {
        Logger.debug(Home.class.getName() + ".changeLanguage(): \n" +
            "    languageId = " + languageId + "\n" +
            "    previous   = " + previous
        );

        Result result = index();

        /* Validate language. */
        if(Language.find.byId(languageId) != null)
        {
            /* OK, change to the new language. */
            session("activeLanId", languageId.toString());

            // TODO: i18n
            flash("success", "Changed language!");

            /*
             * The URL string is not checked, since in case of an invalid
             * URL the home page will be returned.
             */
            result = redirect(new Call("GET", previous, null));
        }
        else
        {
            // TODO: i18n
            flash("error", "Failed to change language!");
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
