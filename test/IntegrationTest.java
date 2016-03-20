import org.junit.*;

import models.Language;
import play.db.Database;
import play.db.Databases;
import play.db.evolutions.Evolutions;

import static play.test.Helpers.*;
import static org.junit.Assert.*;

public class IntegrationTest
{
    private Database database;
    
    @Before
    public void setup()
    {
        //database = Databases.inMemory();
    }
    
    @After
    public void tearDown()
    {
        //database.shutdown();
    }
    
    @Test
    public void testLangauge()
    {
        //Evolutions.applyEvolutions(database);
        
        
        running
        (
            fakeApplication(),
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
                }
            }
        );
        
        
        //Evolutions.cleanupEvolutions(database);
    }

}
