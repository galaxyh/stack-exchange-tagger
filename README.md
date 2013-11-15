stack-exchange-tagger
=====================

This is a tag predictor of text questions from StackExchange.

The goal of this project is to predict tags of text questions from StackExchange(TM). The 7-GB dataset is provided by StackExchange(TM) in the competition held by Facebook on Kaggle.com(TM). Each record in the training data contains 4 columns: Id, Title, Body and Tags. The prediction of tags on test data should only be based on the given Id, Title and Body but not any snooping on the corresponding question on StackExchange(TM).
