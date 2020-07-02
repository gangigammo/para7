package para;

import java.util.Scanner;
import java.util.List;
import java.net.*;
import java.io.*;
import para.graphic.target.*;
import para.graphic.opencl.*;
import para.graphic.shape.*;
import para.graphic.parser.*;
import para.game.*;

public class Game04 extends GameFrame{
  TargetImageFilter inputside,inputside2;
  final Target outputside;
  volatile Thread thread;
  InputStream istream;
  ShapeManager osm;
  ShapeManager ism;
  String serveraddress;
  static final int WIDTH=700;
  static final int HEIGHT=700;
  Target target;

  public Game04(){
    super(new JavaFXCanvasTarget(WIDTH, HEIGHT));
    title="Game04";
    outputside = canvas;
    osm = new OrderedShapeManager();
    ism = new ShapeManager();
  }

  public void init(){
    List<String> params = getParameters().getRaw();
    if (params.size()!=0){
      serveraddress = params.get(0);
    }else{
      serveraddress = "localhost";
    }
  }

  public void gamestart(int v){
    if(thread != null){
      return;
    }
    try{
      Socket socket;
      socket = new Socket(serveraddress, para.game.GameServerFrame.PORTNO);
      istream = socket.getInputStream();
      OutputStream ostream = socket.getOutputStream();
      inputside = new TargetImageFilter(new TextTarget(WIDTH, HEIGHT, ostream),
                                        this, "imagefilter.cl", "Filter9" );
      inputside2 = new TargetImageFilter(new TextTarget(WIDTH, HEIGHT, ostream),
                                        this, "imagefilter.cl", "Filter5" );
      target = new TargetRecorder("record",outputside);

      target.init();
      target.clear();
      //osm.add(new Camera(0,0,300,new Attribute(200,128,128)));
      /*
      Thread thread3 = new Thread(new Runnable(){
        public void run(){
          while(true){
            target.draw(osm);
            target.flush();
            try{
              Thread.sleep(80);
            }catch(InterruptedException e){

            }
          }
        }
      });
      thread3.start();
      */
    }catch(IOException ex){
      System.err.print("To:"+serveraddress+" ");
      System.err.println(ex);
      System.exit(0);
    }

    /* ユーザ入力をサーバに送信するスレッド */
    thread = new Thread(()->{

      //System.out.println(Thread.currentThread().getName());
        int x=150;
        Attribute attr = new Attribute(200,128,128);
        ism.put(new Camera(0, 0, 300,attr));
        ism.put(new Rectangle(v+1, x,30*v+225,60,20,attr));
        inputside.draw(ism);

        /*
        target.clear();
        target.draw(osm);
        target.flush();
        */
        while(true){
          /*
          target.clear()
          target.draw(osm);
          target.flush();
          */
          try{
            Thread.sleep(80);
          }catch(InterruptedException ex){
            thread = null;
            break;
          }
          if((lefton ==1 || righton ==1)){
            x = x-4*lefton+4*righton;
            ism.put(new Rectangle(v+1, x,30*v+225,60,20,attr));
          }
          inputside.setParameter(gamerstate);
          System.out.println(gamerstate);
          if(gamerstate == 50){
            inputside2.draw(ism);
          }else{
            inputside.draw(ism);
          }
          //inputside.draw(ism);
        }
      },"UserInput");
    //System.out.println(Thread.currentThread().getName());
    thread.start();

    /* 受信したデータを画面に出力するスレッド */
    Thread thread2 = new Thread(()->{

        System.out.println(Thread.currentThread().getName());//
        GameMainParser parser = new GameMainParser(this, outputside, osm);
        BufferedReader br = new BufferedReader(new InputStreamReader(istream));
        parser.parse(new Scanner(istream));//loop
        System.out.println("connection closed");
        thread.interrupt();

        while(true){
          target.clear();
          target.draw(osm);
          target.flush();
          try{
            Thread.sleep(80);
          }catch(InterruptedException e){

          }
        }
      });
    thread2.start();
    /*
    Thread thread3 = new Thread(new Runnable(){
      public void run(){
        while(true){
          target.draw(osm);
          target.flush();
          try{
            Thread.sleep(80);
          }catch(InterruptedException e){

          }
        }
      }
    });
    thread3.start();
    */
  }
}
