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

import org.junit.*;

import models.Language;
import static play.test.Helpers.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Integration test class.
 *
 * @author Oliver Dozsa
 */
public class IntegrationTest
{
    private Map<String, Object> config;
    
    
    
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
    
    @Before
    public void setup()
    {
        config = new HashMap<String, Object>();
        
        config.putAll(inMemoryDatabase("default"));
        config.put("play.evolutions.db.default.autoApply", "true");
    }
    
    @After
    public void tearDown()
    {
        
    }
    
    @Test
    public void testLangauge()
    {   
        running
        (
            fakeApplication(config),
            new Runnable()
            {
                public void run()
                {
                    /* Create */
                    String testIsoName = "testIsoName";
                    
                    Language testLang = new Language();
                    
                    testLang.isoName = testIsoName;
                    
                    testLang.save();
                    
                    /* Query */
                    Language dbTestLang = Language.find.where().eq("isoName", testIsoName).findUnique();
                    
                    assertEquals("DB name and test name are not the same!", testIsoName, dbTestLang.isoName);
                    
                    /* Modify */
                    testIsoName = "new test iso name";
                    
                    dbTestLang.isoName = testIsoName;
                    
                    dbTestLang.save();
                    
                    dbTestLang = Language.find.where().eq("isoName", testIsoName).findUnique();
                    
                    assertEquals("DB name and test name are not the same after modification!", testIsoName, dbTestLang.isoName);
                    
                    /* Delete. */
                    
                    assertTrue("Could not delete language!", dbTestLang.delete());
                }
            }
        );
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
