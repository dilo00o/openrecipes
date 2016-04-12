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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import models.Ingredient;
import models.IngredientName;
import models.Language;
import models.Recipe;
import models.RecipeIngredient;
import play.Logger;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import scrapers.IngredientScraper;
import scrapers.data.ScrapedIngredient;
import scrapers.data.ScrapedRecipe;
import scrapers.dbload.DbLoader;
import scrapers.visitors.AprosefVisitor;
import scrapers.visitors.ListBasedVisitor;
import scrapers.visitors.MindmegetteVisitor;
import scrapers.visitors.NosaltyVisitor;
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
    public Result exec_parseIngredients()
    {
       Result result = null;
       
       IngredientScraper ingScraper = new IngredientScraper();
       
       int scrapedNumber = 0;
       
       for(ScrapedIngredient scrapedIng: ingScraper.getScrapedIngredients())
       {
           Ingredient dbIngredient = new Ingredient();
           IngredientName ingName  = new IngredientName();
           
           ingName.name     = scrapedIng.getName();
           ingName.language = Language.find.byId(1L); /* hun */
           
           dbIngredient.names = new ArrayList<IngredientName>();
           dbIngredient.names.add(ingName);
           
           dbIngredient.save();
           
           ingName.ingredient = dbIngredient;
           
           ingName.save();
           
           scrapedNumber++;
       }
       
       flash("success", "saved " + scrapedNumber + " ingredient(s)");
       
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
       
       int startPage = Integer.parseInt(dynamicForm.get("startpage"));
       
       for(int i = 0; i < NUM_OF_SOURCE_SITES; i++)
       {
           String sourceSite = dynamicForm.get("sourcesite_" + i);
           
           if(sourceSite != null)
           {
               sourceSites.add(sourceSite);
           }
       }
       
       Logger.debug(Home.class.getName() + "(admin).exec_parseRecipes():\n" + 
           "    sourceSites = " + sourceSites + "\n" +
           "    startpage = " + startPage
       );
       
       /* Determine which visitors to use. */
       List<ListBasedVisitor> visitors = new ArrayList<ListBasedVisitor>();
       
       for(String site: sourceSites)
       {
           if(site.equalsIgnoreCase("nosalty"))
           {
               visitors.add(new NosaltyVisitor());
           }
           
           if(site.equalsIgnoreCase("aprosef"))
           {
               visitors.add(new AprosefVisitor());
           }
           
           if(site.equalsIgnoreCase("mindmegette"))
           {
               visitors.add(new MindmegetteVisitor());
           }
       }
       
       /* Start parsing. */
       for(ListBasedVisitor visitor: visitors)
       {
           DbLoader loader = new DbLoader(visitor);
           
           loader.load();
       }
       
       result = ok
       (
           parseRecipes.render()
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
