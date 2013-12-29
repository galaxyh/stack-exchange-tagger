hadoop fs -rmr output
nohup hadoop jar CoocurrenceMR.jar org.h2t2.setagger.core.CooccurrenceMapReduce input output model/cooccurrence_head_2000.model > predict_comr.out 2>&1 &
