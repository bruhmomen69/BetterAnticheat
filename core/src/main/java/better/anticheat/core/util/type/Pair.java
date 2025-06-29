package better.anticheat.core.util.type;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Pair<X, Y> {
    private X x;
    private Y y;
}
