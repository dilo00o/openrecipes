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

package controllers.privately;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;

import play.Logger;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.privateviews.*;

/**
 * Controller page for private stuff.
 *
 * @author Oliver Dozsa
 */
public class Home extends Controller
{
    /* --------------------------------------------------------------------- */
    /* ATTRIBUTES                                                            */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC ATTRIBUTES ------------------------------------------------ */
    
    /**
     * Number of source sites.
     */
    public static final int NUM_OF_SOURCE_SITES = 3;



    /* -- PROTECTED ATTRIBUTES --------------------------------------------- */
    
    @Inject FormFactory formFactory;



    /* -- PRIVATE ATTRIBUTES ----------------------------------------------- */



    /* --------------------------------------------------------------------- */
    /* METHODS                                                               */
    /* --------------------------------------------------------------------- */

    /* -- PUBLIC METHODS --------------------------------------------------- */
    
    /**
     * The parse ingredients home page.
     * 
     * @return
     */
    public Result index()
    {
       Result result = null;
       
       result = ok
       (
           home.render()
       );
       
       return result;
    }
    
    /**
     * The parse ingredients home page.
     * 
     * @return
     */
    public Result parseIngredientsHome()
    {
       Result result = null;
       
       result = ok
       (
           parseIngredients.render()
       );
       
       return result;
    }
    
    /**
     * The parse recipes home page.
     * 
     * @return
     */
    public Result parseRecipesHome()
    {
       Result result = null;
       
       result = ok
       (
           parseRecipes.render()
       );
       
       return result;
    }
    
    /**
     * Executes parse ingredients.
     * 
     * @return
     */
    public Result exec_parseIngredientsHome()
    {
       Result result = null;
       
       // TODO
       
       result = ok
       (
           parseIngredients.render()
       );
       
       return result;
    }
    
    /**
     * Executes parse recipes.
     * 
     * @return
     */
    public Result exec_parseRecipes()
    {
       Result result = null;
       
       DynamicForm dynamicForm = formFactory.form().bindFromRequest();
       
       List<String> sourceSites = new ArrayList<String>();
       
       for(int i = 0; i < NUM_OF_SOURCE_SITES; i++)
       {
           String sourceSite = dynamicForm.get("sourcesite_" + i);
           
           if(sourceSite != null)
           {
               sourceSites.add(sourceSite);
           }
       }
       
       Logger.debug(Home.class.getName() + "(admin).exec_parseRecipes():\n" + 
           "    sourceSites = " + sourceSites
       );
       
       for(String site: sourceSites)
       {
           if(site.equalsIgnoreCase("nosalty"))
           {
               // TODO
           }
           
           if(site.equalsIgnoreCase("aprosef"))
           {
               // TODO
           }
           
           if(site.equalsIgnoreCase("mindmegette"))
           {
               // TODO
           }
       }
       
       result = ok
       (
           parseIngredients.render()
       );
       
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
