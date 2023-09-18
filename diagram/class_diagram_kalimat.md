```mermaid
classDiagram
    class SentenceFragment{
        -Analyzer analyzer
        -classify(text String)
    }
    
    class Analyzer {
        -Context context
        -CoroutineScope coroutineScope
        -Map~String, Int~ wordToIndex
        -Interpreter tflite
        -List~String~ labels
        -loadJsonFromAsset(context Context, filename String) String
        -getWordToIndex(context Context) Map~String, Int~ 
        -cleanText(text String) String
        -slangHandling(text String) String
        -removeStopword(text String) String
        -toSequence(text String) Array~Int~
        -padSequence(sequence Array~Int~) Array~Int~
        -preprocess(text String) Array~Int~
        +classify(text String) ClassificationResult
    }
    
    class PredictionResult{
        String label
        Float prob
    }
    
    class AnalyzerResult{
        List~PredictionResult~ predictionResults
        long inferenceTime
        String predictionLabel
        int index
        String trueLabel
        String content
    }


    SentenceFragment "1" *-- Analyzer
    Analyzer "1" .. "1" AnalyzerResult : Instantiate 
    PredictionResult "n" --o "1" AnalyzerResult
```