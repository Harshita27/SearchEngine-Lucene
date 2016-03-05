# SearchEngine-Lucene
A Java project to implement a Lucene based Search Engine
Author: Harshita Agrawal
=============================================================================================
Project Name: LuceneSearchEngine
Package Name: lucenesearchengine

Java Class File:
	LuceneSearchEngine.java: Main Class : 
				No arguments
		
Libraries referenced:
	a) lucene-analyzers-common-4.7.2.jar
	b) lucene-queries-4.7.2.jar
	c) lucene-core-4.7.2.jar
	d) lucene-queryparser-4.7.2.jar
	e) jsoup-1.8.3.jar	

===============================================================================================
CONFIGURTION INSTRUCTIONS:
===============================================================================================

Environment used to build the project: JDK 1.8 , JAVA v.8
Pre-requisite: Java Development Environment

===============================================================================================
HOW TO RUN THE COMPILED JAR :
===============================================================================================

	To run the Jar file:
		java -jar /path/to/the/jar/lucenesearchengine.jar 
	
================================================================================================
ATTACHMENTS:
================================================================================================

1) The working project with source code.
2) 1 jar file lucenesearchengine.jar
3) A sorted (by frequency) list of (term, term_freq pairs) in the file:
	sorted_(by frequency)_list_of_(term-term_freq pairs).xls
4) A plot of the resulting Zipfian curve in the file:
	ZipfsPlot.pdf
5) Four lists (one per query) each containing at MOST 100 docIDs ranked by score in the files:
	Query1_100_docIDs_ranked_by_score.xls
	Query2_100_docIDs_ranked_by_score.xls
	Query3_100_docIDs_ranked_by_score.xls
	Query4_100_docIDs_ranked_by_score.xls
6) A table comparing the total number of documents retrieved per query using Lucene’s scoring 
   function vs. using your search engine (index with BM25) :
	Total_Documents_Comparison_Lucene&BM25.xls
	
================================================================================================

================================================================================================
HOW DOES THE PROGRAM WORK?
================================================================================================
1) Enter the path where the Index will be created.
2) Enter the path of the directory where the files to be indexed are present.
3) Before indexing, remove the HTML markup in the documents using jsoup library.
3) Using lucene's indexing libraries, write the index at the specified location by
   defining Fields such as TextField for contents and StringField for document path.
4) Once the index is written, perform the following tasks:
	a) Get the frequencies for all the terms indexed.
	b) Run the queries 
5) To fetch the frequency of all the terms occuring in the index, read the index file first and
   loop through all the Terms in the index using lucene's library classes TermsEnum, DocsEnum 
   and Terms.
6) Get the frequencies for all the terms using lucene's library method.
7) To search the queries, read the index file and set the lucene's TopDocumentCollector to search 
   for top 100 document hits.
8) Using lucene's ranking and scoring methods, rank the documents and retreive the top 100 document
   hits
   for each query
9) The output files fromt he program run are csv files and are stored in the directory location 
   same as the project folder.

