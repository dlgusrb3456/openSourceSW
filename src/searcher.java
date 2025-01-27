import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.snu.ids.kkma.index.Keyword;
import org.snu.ids.kkma.index.KeywordExtractor;
import org.snu.ids.kkma.index.KeywordList;

import java.io.*;
import java.util.*;

public class searcher {
    private String path="";
    private String findStr="";
    public searcher(String path,String findStr){
        this.path=path;
        this.findStr=findStr;
    }

    public static File[] getFile(String path){
        File dir = new File(path);
        return dir.listFiles();
    }

    public void CalcSim() throws IOException, ClassNotFoundException {
        //get index.post file
        FileInputStream fileStream = new FileInputStream(path);
        ObjectInputStream objectInputStream = new ObjectInputStream(fileStream);
        Object object = objectInputStream.readObject();
        objectInputStream.close();

        HashMap<String,String[]> hashs = (HashMap<String, String[]>) object;
        //makeInverseIndex mI = new makeInverseIndex();
        //mI.rdInverseIndex(object);

        //make kkm list
        KeywordExtractor ke = new KeywordExtractor();
        KeywordList kl = ke.extractKeyword(findStr,true);

        ArrayList<Double[]> findSim = new ArrayList<Double[]>();

        //test kkm
//        for(int i=0;i<kl.size();i++){
//            Keyword kw = kl.get(i);
//            System.out.println(kw.getString()+" "+kw.getCnt());
//        }


        for(int i=0;i<kl.size();i++){
            Keyword kwrd = kl.get(i);
            if(hashs.containsKey(kwrd.getString())) {
                Double[] tmpDoubleArr = new Double[6];
                String[] tmpString = hashs.get(kwrd.getString()); //each file's weight, 0 to 4
                tmpDoubleArr[0] = (double) kwrd.getCnt();
                for (int k = 1; k < 6; k++) {
                    tmpDoubleArr[k] = Double.parseDouble(tmpString[k - 1]);
                }
                findSim.add(tmpDoubleArr);
            }
            else{
                System.out.println("No "+kwrd.getString()+" word in files");
            }

        }
        ArrayList<Double> resultSimInEachFile = new ArrayList<Double>();

        //check findSim arrayList;
//        for(int i=0;i<findSim.size();i++){
//            for(int j=0;j<findSim.get(i).length;j++){
//                System.out.print(findSim.get(i)[j]+"//");
//            }
//            System.out.println();
//        }


        if(findSim.size()>=0){
            for(int i=1;i<findSim.get(0).length;i++){
                Double tmpWeight = 0.0;
                Double cntSquare=0.0;
                Double weightSquare =0.0;
                for(int j=0;j<findSim.size();j++){
                    tmpWeight += findSim.get(j)[0]*findSim.get(j)[i]; //여기까지만 한 값이 innerProduct
                    cntSquare += Math.pow(findSim.get(j)[0],2);
                    weightSquare += Math.pow(findSim.get(j)[i],2);
                }
                //resultSimInEachFile.add(tmpWeight); //이 값은 innerProduct
//                System.out.println("cntSquare: "+Math.sqrt(cntSquare));
//                System.out.println("weightSquare: "+Math.sqrt(weightSquare));
//                System.out.println("tmpWeight: "+tmpWeight);
                if(cntSquare==0.0 || weightSquare==0.0){
                    resultSimInEachFile.add(0.0);
                }
                else{
                    Double resultSim = tmpWeight/(Math.sqrt(cntSquare)*Math.sqrt(weightSquare));
                    //System.out.println("resultSim: "+resultSim);
                    resultSimInEachFile.add(resultSim); //Sim값
                }
            }
        }

        Map<Integer,Double> tmpMap = new HashMap<Integer,Double>(); //Integer = fileIndex, Double = each file's weight

        for(int j=0;j<resultSimInEachFile.size();j++){
            tmpMap.put(j,resultSimInEachFile.get(j));
        }
        //current status: tmpMap have <file index, file weight> of key String
        //to do: sort tmpMap to choose top three file index
        List<Map.Entry<Integer,Double>> list_entries = new ArrayList<Map.Entry<Integer, Double>>(tmpMap.entrySet());
        Collections.sort(list_entries, new Comparator<Map.Entry<Integer,Double>>(){

            @Override
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        int k=0;
        String answer="";
        for(Map.Entry<Integer,Double> entry: list_entries){
            if(k>=3){
                break;
            }
            k++;
            String title = getTitle(entry.getKey());
            if(entry.getValue() != 0.0){
                answer += Integer.toString(k)+"등:"+title+", 유사도: "+entry.getValue()+" / ";
            }

        }
        System.out.println(answer);
    }

    public String getTitle(int index) throws IOException {
        //get collection file to get title
        File collectionFile = new File("./src/collection.xml");
        Document fileDoc = Jsoup.parse(collectionFile, "UTF-8");
        Elements els = fileDoc.select("doc");
        return els.get(index).select("title").text();

    }

}
