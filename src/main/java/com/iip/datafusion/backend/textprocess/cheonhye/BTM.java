package com.iip.datafusion.backend.textprocess.cheonhye;

import java.io.*;
import java.text.NumberFormat;
import java.util.*;


/*
@ Author: luqianhui
@ Function ： topic model

 */
public class BTM  {
    private final String data_path;
    private final double alpha;
    private final double beta;
    private final int iter_num;
    private final int topic_num;
    private final String outputPath;

    private HashMap<String, Integer> wordMap = new HashMap<>();
    private HashMap<Integer, String> id2Word = new HashMap<>();
    private int worMapSize = 0;

    private int[][] wordId_of_corpus = null;
    //    private int[][] topicId_of_corpus = null;
//
//    private int[][] word_num_in_topic_word = null;
//    private int[] word_num_in_topic = null;
//    private int[][] word_num_in_doc_topic = null;
//    private int[] word_num_in_doc = null;
    ArrayList<HashMap<Long, Integer>> biterm_of_corpus = new ArrayList<>();
    int[] doc_biterm_num ;
    ArrayList<Long> biterms = new ArrayList<>();


    private int[] topic_of_biterms;
    private int[][] topic_word_num;
    private int[] num_of_topic_of_biterm;

    private HashMap<Long, Double> bitermSum = new HashMap<>();

    public BTM(String data_path, int topic_num, int iter_num, double alpha, double beta, int instanceNum) {

        this.alpha = alpha;
        this.beta = beta;
        this.iter_num = iter_num;
        this.topic_num = topic_num;
        this.data_path = data_path;
        this.outputPath = "./BTM_output/" ;
        System.out.println("BTM_outputPath:"+this.outputPath);
        (new File(this.outputPath)).mkdirs();
        print_parameter();
    }

    public static BufferedReader getReader(String path, String charset) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(path), charset));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return reader;
    }

    private static BufferedWriter getWriter(String path, String charset) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(path), charset));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return writer;
    }

    public static BufferedReader getReader(String path) {
        return getReader(path, "UTF-8");
    }

    public static BufferedWriter getWriter(String path) {
        return getWriter(path, "UTF-8");
    }
    private void print_parameter() {
        System.out.println("input_path:" + this.data_path
                + "\talpha:" + this.alpha
                + "\tbeta:" + this.beta
                + "\titer_num:" + this.iter_num
                + "\ttopic_num:" + this.topic_num
        );
    }

    private void load_data() {
        try {
            BufferedReader reader = getReader(this.data_path);
            String line;
            ArrayList<int[]> tmpCorpus = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                String[] words = line.split("\\s+");
                int[] oneDoc = new int[words.length];
                for (int wordIndex = 0; wordIndex < words.length; wordIndex++) {
                    if (!this.wordMap.containsKey(words[wordIndex])) {
                        this.wordMap.put(words[wordIndex], this.wordMap.size());
                        this.id2Word.put(this.wordMap.get(words[wordIndex]), words[wordIndex]);
                    }
                    oneDoc[wordIndex] = this.wordMap.get(words[wordIndex]);
                }
                tmpCorpus.add(oneDoc);
            }
            reader.close();

            this.doc_biterm_num = new int[tmpCorpus.size()];
            this.wordId_of_corpus = new int[tmpCorpus.size()][];
            for (int docIndex = 0; docIndex < this.wordId_of_corpus.length; docIndex++) {
                this.wordId_of_corpus[docIndex] = tmpCorpus.get(docIndex);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void init_model() {
        int docIndex = 0;
        for(int[] doc:this.wordId_of_corpus){
            HashMap<Long, Integer> oneCop = new HashMap<>();
            for(int word1:doc){
                for(int word2:doc){
                    if(word1<word2){
                        Long itmeNum = (long)word1*1000000+word2;
                        if(!oneCop.containsKey(itmeNum)){
                            oneCop.put(itmeNum,0);
                        }
                        oneCop.put(itmeNum,oneCop.get(itmeNum)+1);
                        this.biterms.add(itmeNum);
                        this.doc_biterm_num[docIndex] += 1;
                    }
                }
            }
            docIndex++;
            this.biterm_of_corpus.add(oneCop);
        }

        this.worMapSize = this.wordMap.size();

        this.topic_of_biterms = new int[this.biterms.size()];
        this.topic_word_num = new int[this.worMapSize][this.topic_num];
        this.num_of_topic_of_biterm = new int[this.topic_num];

        for(int bitermIndex=0;bitermIndex<this.biterms.size();bitermIndex++){
            int topicId = (int) (Math.random() * this.topic_num);
            this.topic_word_num[(int)(this.biterms.get(bitermIndex)%1000000)][topicId] += 1;
            this.topic_word_num[(int)(this.biterms.get(bitermIndex)/1000000)][topicId] += 1;
            this.num_of_topic_of_biterm[topicId] += 1;
            this.topic_of_biterms[bitermIndex] = topicId;
        }


    }

    private void save_twords(int topWordNum) throws IOException {
        BufferedWriter writer = getWriter(this.outputPath + "model-final_twords.txt");

        for (int topic_id = 0; topic_id < this.topic_num; topic_id++) {
            HashMap<Integer, Double> oneLine = new HashMap<>();
            for (int word_id = 0; word_id < this.worMapSize; word_id++) {
                oneLine.put(word_id, ((double) this.topic_word_num[word_id][topic_id]) / this.num_of_topic_of_biterm[topic_id] / 2);
            }
            List<Map.Entry<Integer, Double>> maplist = new ArrayList<>(oneLine.entrySet());

            Collections.sort(maplist, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));

            writer.write("Topic:" + topic_id + "\n");
            int count = 0;
            for (Map.Entry<Integer, Double> o1 : maplist) {
                writer.write("\t" + this.id2Word.get(o1.getKey()) + "\t:" + o1.getValue() + "\n");
                count++;
                if (count > topWordNum) {
                    break;
                }
            }
            writer.write("\n");

        }
        writer.close();
    }

    private void save_wordMap() throws IOException {
        BufferedWriter writer = getWriter(this.outputPath + "model-final_wordmap.txt");
        for (String key : this.wordMap.keySet()) {
            writer.write(key + " " + this.wordMap.get(key));
            writer.write("\n");
        }
        writer.close();
    }


    private double getSum(Long biterm){
        if(!bitermSum.containsKey(biterm)) {
            double sum = 0;
            int word1 = (int)(biterm/1000000);
            int word2 = (int)(biterm%1000000);
            for (int topic_id = 0; topic_id < this.topic_num; topic_id++) {
                sum += (this.num_of_topic_of_biterm[topic_id] + alpha)
                        * (this.topic_word_num[word1][topic_id] + beta)
                        * (this.topic_word_num[word2][topic_id] + beta)
                        / Math.pow(this.num_of_topic_of_biterm[topic_id] * 2 + this.worMapSize * beta, 2);
            }
            bitermSum.put(biterm,sum);
        }
        return bitermSum.get(biterm);
    }




    //用来保留四位小数
    public String formatDouble(double f) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(4);
        return nf.format(f);
    }




    private Map<String,List<String>> save_theta(List<String> filelist) throws IOException {

        Map<String,List<String>> map =new HashMap<String,List<String>>();
        BufferedWriter writer = getWriter(this.outputPath + "model-final_theta.txt");
        int docIndex = 0;
        for (HashMap<Long,Integer> line : this.biterm_of_corpus) {
            double[] oneTheta = new double[this.topic_num];
            List<String> list=new ArrayList<String>();
            for(int topic_id = 0; topic_id<this.topic_num;topic_id++) {

                double oneSum=0;

                for (Long biterm : line.keySet()) {

                    int word1 = (int)(biterm/1000000);
                    int word2 = (int)(biterm%1000000);
                    oneSum+=(((double)line.get(biterm))/this.doc_biterm_num[docIndex])
                            *((
                            (this.num_of_topic_of_biterm[topic_id] + alpha)
                                    * (this.topic_word_num[word1][topic_id] + beta)
                                    * (this.topic_word_num[word2][topic_id] + beta)
                                    / Math.pow(this.num_of_topic_of_biterm[topic_id]*2 + this.worMapSize * beta, 2)
                    )/(getSum(biterm)));

                }
                writer.write( formatDouble(oneSum)+ " ");
                list.add(formatDouble(oneSum));

            }
//            String[] tmp_string=filelist.get(docIndex).split("/");
//            int tmp_len=tmp_string.length;
            String stringName = filelist.get(docIndex);
            map.put(stringName,list);
            writer.write("\n");
            docIndex++;
        }
        writer.close();
        return map;
    }

    private void save_phi() throws IOException {
        BufferedWriter writer = getWriter(this.outputPath + "model-final_phi.txt");
        int topic_index = 0;
        for (int topic_id = 0; topic_id < this.topic_num; topic_id++) {
            for (int word_id = 0; word_id < worMapSize; word_id++) {
                writer.write(((this.topic_word_num[word_id][topic_id] + beta) / (this.num_of_topic_of_biterm[topic_id] * 2 + worMapSize * beta))+" ");
            }

            writer.write("\n");
        }
        writer.close();

    }


    private void build_model() {

        for (int iter = 0; iter < this.iter_num; iter++) {
            long startTime = System.currentTimeMillis();

            for(int bitermIndex = 0;bitermIndex<this.topic_of_biterms.length;bitermIndex++) {
                int oldTopicId = this.topic_of_biterms[bitermIndex];
                int word1 = (int)(this.biterms.get(bitermIndex)/1000000);
                int word2 = (int)(this.biterms.get(bitermIndex)%1000000);
                this.topic_word_num[word1][oldTopicId] -= 1;
                this.topic_word_num[word2][oldTopicId] -= 1;
                this.num_of_topic_of_biterm[oldTopicId] -= 1;

                int newTopicId = -1;

                double[] p = new double[this.topic_num];
                for (int k = 0; k < this.topic_num; k++) {
                    p[k] = (this.num_of_topic_of_biterm[k] + alpha)
                            * (this.topic_word_num[word1][k] + beta)
                            * (this.topic_word_num[word2][k] + beta)
                            / Math.pow(this.num_of_topic_of_biterm[k]*2 + this.worMapSize * beta, 2);
                }

                for (int k = 1; k < this.topic_num; k++) {
                    p[k] += p[k - 1];
                }

                double u = Math.random() * p[this.topic_num - 1];
                for (int k = 0; k < this.topic_num; k++) {
                    if (u < p[k]) {
                        newTopicId = k;
                        break;
                    }

                }
                this.topic_word_num[word1][newTopicId] += 1;
                this.topic_word_num[word2][newTopicId] += 1;
                this.num_of_topic_of_biterm[newTopicId] += 1;

                this.topic_of_biterms[bitermIndex] = newTopicId;
            }

            System.out.println("finished iter :" + iter + "\tcost time:" + ((double) System.currentTimeMillis() - startTime) / 1000);
        }

    }

    private Map<String,List<String>> save_result(List<String> filelist)throws IOException {

            Map<String,List<String>> map =new HashMap<String,List<String>>();

            this.save_twords(20);
            map=this.save_theta(filelist);
//            this.save_tassign();
            this.save_wordMap();
            this.save_phi();
            return map;


    }

   // @Override
    public Map<String,List<String>> execute(List<String> filelist)throws IOException {
        Map<String,List<String>> map =new HashMap<String,List<String>>();
        this.load_data();
        this.init_model();
        this.build_model();
        map=this.save_result(filelist);
        return map;
    }

    public static Map<String,List<String>> build_BTM_Model(String data_path, int topic_num, double alpha, double beta, int iter_num,  int instance_num,List<String> filelist)throws IOException
    {
            Map<String,List<String>> map =new HashMap<String,List<String>>();
            BTM btm=new BTM(data_path, topic_num, iter_num, alpha, beta, instance_num);
            map=btm.execute(filelist);
            return map;

    }
    public static Map<String,List<String>> btm(String dirc,int tN)throws Exception{
        Map<String,List<String>> map =new HashMap<String,List<String>>();
        List<String> filelist=BTM_PreProceed.toNormal(dirc);
        map=build_BTM_Model("./toNormal.txt",tN,0.1,0.01,500,1,filelist);
        return map;

    }

    public static void main(String[] args) throws Exception{
        Map<String,List<String>> map =new HashMap<String,List<String>>();   // 文件名 -> 主题List
        String dirc="ganjun_testdata";
        int tN=5;
        map=btm(dirc,tN);
        System.out.println(map);
    }
}

