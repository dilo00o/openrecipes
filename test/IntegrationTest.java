import org.junit.*;

import models.Ingredient;
import models.IngredientName;
import models.Language;
import play.db.Database;
import play.db.Databases;
import play.db.evolutions.Evolutions;

import static play.test.Helpers.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class IntegrationTest
{
    private Map<String, Object> config;
    
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
    
    @Test
    public void testIngredient()
    {
        running
        (
            fakeApplication(config),
            new Runnable()
            {
                public void run()
                {
                    /* Create. */
                    String testIngNameStr = "testIngName";
                    
                    Ingredient testIngredient = new Ingredient();
                    
                    testIngredient.save();
                    
                    IngredientName testIngName = new IngredientName();
                    
                    testIngName.ingredient = testIngredient;
                    testIngName.name       = testIngNameStr;
                    
                    // TODO
                    
                    
                    /* Modify. */
                    
                    
                    /* Delete. */
                }
            }
        );
    }
}
