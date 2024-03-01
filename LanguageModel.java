import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;

    // The window length used in this model.
    int windowLength;

    // The random number generator used by this model. 
    private Random randomGenerator;

    /**
     * Constructs a language model with the given window length and a given
     * seed value. Generating texts from this model multiple times with the
     * same seed value will produce the same random texts. Good for debugging.
     */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /**
     * Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production.
     */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /**
     * Builds a language model from the text in the given file (the corpus).
     */
    public void train(String fileName) {
        String window = "";
        char c;
        In in = new In(fileName);
        for (int i = 0; i < windowLength; i++) { //creates the first window
            window += in.readChar();
        }

        while (!in.isEmpty()) {
            c = in.readChar();
            List probs;
            if (CharDataMap.containsKey(window)) {
                probs = CharDataMap.get(window);
            } else {
                probs = new List();
                CharDataMap.put(window, probs);
            }
            probs.update(c);
            window = window.substring(1, windowLength);
            window += c;
        }
        for (List probs : CharDataMap.values()) {
            calculateProbabilities(probs);
        }
    }

    // Computes and sets the probabilities (p and cp fields) of all the
    // characters in the given list. */
    public void calculateProbabilities(List probs) {
        ListIterator li = probs.listIterator(0);
        int charCount = 0;
        while (li.hasNext()) {
            charCount += li.next().count;
        }
        double probability = 1 / (double) charCount;
        li = probs.listIterator(0);
        double cumulativeProbability = 0;
        while (li.hasNext()) {
            CharData current = li.next();
            current.p = current.count * probability;
            cumulativeProbability += current.p;
            current.cp = cumulativeProbability;
        }
    }

    // Returns a random character from the given probabilities list.
    public char getRandomChar(List probs) {
        double rnd = randomGenerator.nextDouble();
        ListIterator li = probs.listIterator(0);
        CharData cd = li.next();
        while (cd != null && rnd > cd.cp) {
            cd = li.next();
        }
        return cd.chr;
    }

    /**
     * Generates a random text, based on the probabilities that were learned during training.
     *
     * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
     *                    doesn't appear as a key in Map, we generate no text and return only the initial text.
     * @return the generated text
     */
    public String generate(String initialText, int textLength) {
        if (initialText.length() < windowLength) {
            return initialText;
        }
        String generatedText = initialText;
        String window = initialText.substring(initialText.length() - windowLength);
        List probs = null;
        for (int i = 0; i < textLength; i++) {
            probs = CharDataMap.get(window);
            if (probs == null) {
                return generatedText;
            }
            char randomChar = getRandomChar(probs);
            generatedText += randomChar;
            window = window.substring(1, windowLength);
            window += randomChar;

        }
        return generatedText;
    }

    /**
     * Returns a string representing the map of this language model.
     */
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (String key : CharDataMap.keySet()) {
            List keyProbs = CharDataMap.get(key);
            str.append(key + " : " + keyProbs + "\n");
        }
        return str.toString();
    }

    public static void main(String[] args) {
        //LanguageModel lm = new LanguageModel(7, 20);
        //lm.train("originofspecies.txt");
        //System.out.println(lm.generate("Natural", 172));
        int windowLength = Integer.parseInt(args[0]);
        String initialText = args[1];
        int generatedTextLength = Integer.parseInt(args[2]);
        Boolean randomGeneration = args[3].equals("random");
        String fileName = args[4];
        // Create the LanguageModel object
        LanguageModel lm;
        if (randomGeneration)
            lm = new LanguageModel(windowLength);
        else
            lm = new LanguageModel(windowLength, 20);
// Trains the model, creating the map.
        lm.train(fileName);
// Generates text, and prints it.
        System.out.println(lm.generate(initialText, generatedTextLength));
    }
}
