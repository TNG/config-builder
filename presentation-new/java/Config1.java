// not shown
import com.tngtech.config.builder.*;

// shown
public class Config {
    public static void main() {
        new ConfigBuilder<>(Config.class).build();
    }

    @DefaultValue("Wolverine")
    private String characterName;
    
    @FooValue()
    private int x;   
}