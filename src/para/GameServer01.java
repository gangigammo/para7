package para;

import java.io.IOException;
import java.util.stream.IntStream;

import para.graphic.shape.Rectangle;
import para.graphic.shape.Attribute;
import para.graphic.shape.ShapeManager;
import para.graphic.shape.OrderedShapeManager;
import para.graphic.shape.Vec2;
import para.graphic.shape.MathUtil;
import para.graphic.shape.Shape;
import para.graphic.shape.Circle;
import para.graphic.shape.Digit;
import para.graphic.shape.CollisionChecker;
import para.graphic.target.TranslateTarget;
import para.graphic.target.TranslationRule;
import para.game.GameServerFrame;
import para.game.GameInputThread;
import para.game.GameTextTarget;

public class GameServer01{
  final Attribute wallattr = new Attribute(250,230,200,true,0,0,0);
  final Attribute ballattr = new Attribute(250,120,120,true,0,0,0);
  final Attribute scoreattr = new Attribute(60,60,60,true,0,0,0);
  final int MAXCONNECTION=2;
  final GameServerFrame gsf;
  final ShapeManager[] userinput;
  final ShapeManager[] wall;
  final ShapeManager[] blocks;
  final ShapeManager[] ballandscore;
  final Vec2[] pos;
  final Vec2[] vel;
  final int[] score;
  final CollisionChecker checker;
  final int MAXSCORE = 50;
  boolean isend,flag = false;
  float count;
  int storei;
  GameTextTarget out1,out2;

  private GameServer01(){
    isend = false;
    checker = new CollisionChecker();
    gsf = new GameServerFrame(MAXCONNECTION);
    userinput = new ShapeManager[MAXCONNECTION];
    wall = new OrderedShapeManager[MAXCONNECTION];
    blocks = new OrderedShapeManager[MAXCONNECTION];
    ballandscore = new ShapeManager[MAXCONNECTION];
    pos = new Vec2[MAXCONNECTION];
    vel = new Vec2[MAXCONNECTION];
    score = new int[MAXCONNECTION];
    for(int i=0;i<userinput.length;i++){
      userinput[i] = new ShapeManager();
      ballandscore[i] = new ShapeManager();
      wall[i] = new OrderedShapeManager();
      blocks[i] = new OrderedShapeManager();
      pos[i] = new Vec2(i*350+150,200);
      vel[i] = new Vec2(0,0);
    }
  }

  public void start(){
    try{
      gsf.init();
    }catch(IOException ex){
      System.err.println(ex);
    }
    gsf.welcome();

    int gs=1;
    while(true){
      //gs = 1;
      //gs = (gs+50)%350;//
      GameInputThread git = gsf.queue.poll();
      if(git != null){
        int id = git.getUserID();
        init(id);
        startReceiver(git);
      }
      try{
        Thread.sleep(100);
      }catch(InterruptedException ex){

      }
      for(int i=0;i<MAXCONNECTION;i++){
        GameTextTarget out = gsf.getUserOutput(i);
        if(i == 0){
          out1 = out;
        }else{
          out2 = out;
        }
        if(out != null){
          /*
          if(i == MAXCONNECTION-1){
            for(int j=0;j<MAXCONNECTION;j++){
            calcForOneUser(j);
            ballandscore[j].put(new Circle(j*10000+1, (int)pos[j].data[0],
                                   (int)pos[j].data[1], 5, ballattr));
            putScore(j,score[j]);
            out.gamerstate(gs); //Gamerの状態をクライアントに伝える
            distributeOutput(out);
            }
          }
          */

          calcForOneUser(i);
          ballandscore[i].put(new Circle(i*10000+1, (int)pos[i].data[0],
                                 (int)pos[i].data[1], 5, ballattr));
          putScore(i,score[i]);
          out.gamerstate(gs); //Gamerの状態をクライアントに伝える
          distributeOutput(out);
          if(score[i] == MAXSCORE /*&& isend == true*/){
            flag = true;
            gs = 200;
            distributeOutput(out);
            break;
          }
        }
      }
      if(flag == true){
        gs = 200;
        break;
      }

      /*
      System.out.println(count);
      if(count >= 300){
        for(int j=0 ; j<MAXCONNECTION;j++){
          GameTextTarget out = gsf.getUserOutput(j);
          out = gsf.getUserOutput(j);
          out.finish();
        }
      }
      */
    }
    for(int i=0;i<MAXCONNECTION;i++){
      GameTextTarget out = gsf.getUserOutput(i);
      //if(out != null){
        /*
        if(i == MAXCONNECTION-1){
          for(int j=0;j<MAXCONNECTION;j++){
          calcForOneUser(j);
          ballandscore[j].put(new Circle(j*10000+1, (int)pos[j].data[0],
                                 (int)pos[j].data[1], 5, ballattr));
          putScore(j,score[j]);
          out.gamerstate(gs); //Gamerの状態をクライアントに伝える
          distributeOutput(out);
          }
        }
        */

        calcForOneUser(i);
        ballandscore[i].put(new Circle(i*10000+1, (int)pos[i].data[0],
                               (int)pos[i].data[1], 5, ballattr));
        putScore(i,score[i]);
        out.gamerstate(gs); //Gamerの状態をクライアントに伝える
        distributeOutput(out);
      //}
    }

    for(int j=0 ; j<MAXCONNECTION;j++){
      GameTextTarget out = gsf.getUserOutput(j);
      out.gamerstate(gs);
      Thread thread = new Thread(new Runnable(){
        public void run(){
          GameTextTarget out1,out2;
          if(score[0] < score[1]){

            out1 = gsf.getUserOutput(0);
            distributeOutput(out1);
            out1.gamerstate(200);

            out2 = gsf.getUserOutput(1);
            distributeOutput(out2);
            out2.gamerstate(0);

            //out.gamerstate(gs);
          }else{

            out1 = gsf.getUserOutput(1);
            distributeOutput(out1);
            out1.gamerstate(0);


            out2 = gsf.getUserOutput(0);
            distributeOutput(out2);
            out2.gamerstate(200);

          }
          //distributeOutput(out);
          //out.gamerstate(200);
          try{
            Thread.sleep(1000);
          }catch(InterruptedException e){

          }
          for(int j=0 ; j<MAXCONNECTION;j++){
            GameTextTarget out = gsf.getUserOutput(j);
          out.finish();
        }
        }
      });
      thread.start();
      //distributeOutput(out);
      //out.gamerstate(gs); //Gamerの状態をクライアントに伝える
      //out.finish();
    }

  }

  private void startReceiver(GameInputThread git){
    int id = git.getUserID();
    git.init(new TranslateTarget(userinput[id],
                    new TranslationRule(id*10000,new Vec2(id*350,0))),
             new ShapeManager[]{userinput[id],wall[id],
                                blocks[id],ballandscore[id]}
             );
    git.start();
  }

  private void init(int id){
    wall[id].add(new Rectangle(id*10000+5, id*350+0, 0, 320, 20, wallattr));
    wall[id].add(new Rectangle(id*10000+6, id*350+0, 0, 20, 300, wallattr));
    wall[id].add(new Rectangle(id*10000+7, id*350+300,0, 20, 300, wallattr));
    wall[id].add(new Rectangle(id*10000+8, id*350+0,281, 320, 20, wallattr));
    IntStream.range(0,33*20).forEach(n->{
        int x = n%33;
        int y = n/33;
        blocks[id].add(new Rectangle(id*10000+n,id*350+30+x*8,30+y*8,6,6,
                             new Attribute(250,100,250,true,0,0,0)));
      });

    pos[id] = new Vec2(id*350+150,200);
    if(id == MAXCONNECTION-1){
      for(int i = 0;i < MAXCONNECTION;i++){
        vel[i] = new Vec2(12,-36);
      }
    }
    //vel[id] = new Vec2(4,-12);
    score[id] = 0;
  }

  private void calcForOneUser(int id){
    float[] btime = new float[]{1.0f};
    float[] stime = new float[]{1.0f};
    float[] wtime = new float[]{1.0f};
    float time = 1.0f;
    while(0<time){
      if(flag == true){
        break;
      }
      count = time;
      btime[0] = time;
      stime[0] = time;
      wtime[0] = time;
      Vec2 tmpbpos = new Vec2(pos[id]);
      Vec2 tmpbvel = new Vec2(vel[id]);
      Vec2 tmpspos = new Vec2(pos[id]);
      Vec2 tmpsvel = new Vec2(vel[id]);
      Vec2 tmpwpos = new Vec2(pos[id]);
      Vec2 tmpwvel = new Vec2(vel[id]);
      Shape b=checker.check(userinput[id], tmpbpos, tmpbvel, btime);
      Shape s=checker.check(blocks[id], tmpspos, tmpsvel, stime);
      Shape w=checker.check(wall[id], tmpwpos, tmpwvel, wtime);
      if( b != null &&
          (s == null || stime[0]<btime[0]) &&
          (w == null || wtime[0]<btime[0])){
        pos[id] = tmpbpos;
        vel[id] = tmpbvel;
        time = btime[0];
      }else if(s != null){
        blocks[id].remove(s); // block hit!
        score[id]++;
        pos[id] = tmpspos;
        vel[id] = tmpsvel;
        time = stime[0];
        if(score[id] == MAXSCORE){ //
          for(int i = 0;i < MAXCONNECTION;i++){
            vel[i] = new Vec2(0,0);
            //isend = true;
          }
        }
      }else if(w != null){
        pos[id] = tmpwpos;
        vel[id] = tmpwvel;
        time = wtime[0];
        //System.out.println(pos[id].data[1]);
        if(pos[id].data[1] > 280){
          //System.out.println(pos[id].data[1]);
          score[id]--;
        }
      }else{
        if(vel[0].data[0] == 0){
          isend = true;
        }
        pos[id] = MathUtil.plus(pos[id], MathUtil.times(vel[id],time));
        time = 0;
      }
    }
  }

  private void putScore(int id, int score){
    int one = score%10;
    int ten = (score/10)%10;
    ballandscore[id].put(new Digit(id*10000+2,id*300+250,330,20,one,scoreattr));
    ballandscore[id].put(new Digit(id*10000+3,id*300+200,330,20,ten,scoreattr));
  }

  private void distributeOutput(GameTextTarget out){
    if(out == null){
      return;
    }
    out.clear();
    for(int i=0;i<MAXCONNECTION;i++){
      if(gsf.getUserOutput(i)!=null){
        out.draw(wall[i]);
        out.draw(blocks[i]);
        out.draw(userinput[i]);
        out.draw(ballandscore[i]);
      }
    }
    out.flush();
  }

  public static void main(String[] args){
    GameServer01 gs = new GameServer01();
    gs.start();
  }
}
