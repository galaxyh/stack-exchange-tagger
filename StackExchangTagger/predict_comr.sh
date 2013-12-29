hadoop fs -rmr /output
hadoop jar CoocurrenceMR.jar org.h2t2.setagger.core.CooccurrenceMapReduce /input /output /model/cooccurrence_head_2000.model
