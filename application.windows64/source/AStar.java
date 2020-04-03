import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class AStar extends PApplet {

int startingObsticles=50;//number of randomly generated obsticles
int cellSize=10;//pixel width of each cell
int[] obsticlesColor={255,255,0};

int[] startPos={-1,-1};//invalid starting location
int[] endPos={-1,-1};//invalid ending location
boolean started=false;
boolean done=false;
int pathLength;

Node[][] nodes;
Node[] open;//stores the x and y of open nodes
Node[] closed;//stores the x and y of closed nodes
int minF;//stores position of item in open with lowest F

boolean longMode=true;
boolean show=true;

public void setup(){
  
  nodes=new Node[PApplet.parseInt(width/cellSize)][PApplet.parseInt(height/cellSize)];
  for(int i=0;i<PApplet.parseInt(width/cellSize);i++){
    for(int j=0;j<PApplet.parseInt(height/cellSize);j++){
      nodes[i][j]=new Node(i,j);
    }
  }
  int obsticlesMade=0;
  int tempX;
  int tempY;
  while (obsticlesMade<startingObsticles){
    tempX=PApplet.parseInt(random(width/cellSize));
    tempY=PApplet.parseInt(random(width/cellSize));
    if(nodes[tempX][tempY].passable){
      nodes[tempX][tempY].passable=false;
      obsticlesMade+=1;
    }
  }
}

public void draw(){
  clear();
  background(30);
  noStroke();
  
  if((longMode&&show)&&started&&!done){
    step();
  }
  
  for(int i=0;i<width/cellSize;i++){
    for(int j=0;j<height/cellSize;j++){
      if(!nodes[i][j].passable){
        fill(obsticlesColor[0],obsticlesColor[1],obsticlesColor[2]);
      }
      else if(startPos[0]==i&&startPos[1]==j){
        fill(0,255,0);
      }
      else if(endPos[0]==i&&endPos[1]==j){
        fill(255,0,0);
      }
      else if(nodes[i][j].onPath){
        fill(255,255,255);
      }
      else if(nodes[i][j].open&&show){
        fill(0,0,255);
      }
      else if(nodes[i][j].closed&&show){
        fill(255,0,255);
      }
      else{
        fill(0);
      }
      rect(i*cellSize,j*cellSize,cellSize,cellSize);
    }
  }
  
  if(mousePressed&&mouseX<=PApplet.parseInt(width/cellSize)*cellSize&&mouseY<=PApplet.parseInt(height/cellSize)*cellSize&&mouseX>0&&mouseY>0){
    if (key == CODED&&keyCode == SHIFT&&!started&&keyPressed&&(done||!started)){
      if(nodes[PApplet.parseInt(mouseX/cellSize)][PApplet.parseInt(mouseY/cellSize)].passable){
        if(mouseButton == LEFT){
          startPos[0]=PApplet.parseInt(mouseX/cellSize);
          startPos[1]=PApplet.parseInt(mouseY/cellSize);
        }
        else if(mouseButton == RIGHT){
          endPos[0]=PApplet.parseInt(mouseX/cellSize);
          endPos[1]=PApplet.parseInt(mouseY/cellSize);
        }
      }
    }
    else{
      if(mouseButton == LEFT){
        nodes[PApplet.parseInt(mouseX/cellSize)][PApplet.parseInt(mouseY/cellSize)].passable=false;
      }
      else if(mouseButton == RIGHT){
        nodes[PApplet.parseInt(mouseX/cellSize)][PApplet.parseInt(mouseY/cellSize)].passable=true;
      }
    }
  }  
}

public void keyPressed(){
  if(key==' '&&(done||!started)){
    if(startPos[0]==-1||endPos[0]==-1){return;}
    if(startPos[0]==endPos[0]&&startPos[1]==endPos[1]){return;}
    startPathfinding();
  }
  else if(key=='s'){
    show=!show;
    if(!show){
      startPathfinding();
    }
  }
}

public void startPathfinding(){
  started=true;
  done=false;
  //calculate h for all nodes and clear open and closed
  for(int i=0;i<nodes.length;i++){
    for(int j=0;j<nodes[i].length;j++){
      nodes[i][j].setH();
      nodes[i][j].open=false;
      nodes[i][j].closed=false;
      nodes[i][j].onPath=false;
    }
  }
  
  nodes[startPos[0]][startPos[1]].open=true;
  nodes[startPos[0]][startPos[1]].f=0;
  nodes[startPos[0]][startPos[1]].g=0;
  open=new Node[1];
  open[0]=nodes[startPos[0]][startPos[1]];
  closed=new Node[0];
  
  while(!done&&((!longMode)||(!show))){
    step();
    
  }
}
public void step(){
  Node[] temp;
  boolean moved;
  minF=0;//assume first open is minimum
  for(int i=1;i<open.length;i++){
    if(open[i].f<open[minF].f){
      minF=i;
    }
    else if(open[i].f==open[minF].f){
      if(open[i].h<open[minF].h){
        minF=i;
      }
    }
  }
  explore(open[minF].x,open[minF].y);
  //adds the chosen one to closed
  open[minF].closed=true;
  open[minF].open=false;
  temp=new Node[closed.length+1];
  for(int i=0;i<closed.length;i++){
    temp[i]=closed[i];
  }
  temp[closed.length]=open[minF];
  closed=temp;
  //this section removes the cosen one from open
  open[minF].open=false;
  moved=false;
  temp=new Node[open.length-1];
  for(int i=0;i<open.length;i++){
    if(i==minF){
      moved=true;
      continue;
    }
    temp[i+(-1*PApplet.parseInt(moved))]=open[i];
  }
  open=temp;
}

public void explore(int x,int y){//explores node at x y
  
  for(int i=x-1;i<x+2;i+=2){
    if(i<0||i>=PApplet.parseInt(width/cellSize)){continue;}
    if(!nodes[i][y].closed&&nodes[i][y].passable){
      reveal(nodes[i][y],nodes[x][y]);
    }
  }
  for(int i=y-1;i<y+2;i+=2){
    if(i<0||i>=PApplet.parseInt(height/cellSize)){continue;}
    if(!nodes[x][i].closed&&nodes[x][i].passable){
      reveal(nodes[x][i],nodes[x][y]);
    }
  }
}
public void reveal(Node subject,Node source){//reveal the subject
  Node[] temp;
  if(subject.x==endPos[0]&&subject.y==endPos[1]){
    subject.source=source;
    done=true;
    started=false;
    finishUp();
  }
  if(subject.open){
    if(source.g+1<subject.g){
      subject.g=source.g+1;
      subject.source=source;
    }
  }
  else{
    subject.open=true;
    temp=new Node[open.length+1];
    for(int i=0;i<open.length;i++){
      temp[i]=open[i];
    }
    temp[open.length]=subject;
    open=temp;
    
    subject.source=source;
    subject.g=source.g+1;
  }
  subject.f=subject.h+subject.g;
}

public void finishUp(){
  pathLength=0;
  Node temp=nodes[endPos[0]][endPos[1]].source;
  while(!(temp.x==startPos[0]&&temp.y==startPos[1])){
    pathLength+=1;
    temp.onPath=true;
    temp=temp.source;
  }
}
class Node{
  
  boolean passable;
  boolean open;//if th is node has been evaluated from another node
  boolean closed;//if this node has been the source for an evaluation
  float h;//heruistic cost to target
  float g;//cost to get to node from start
  float f;//total apeal of node
  int x;//x location in grid
  int y;//y location in grid
  boolean onPath;
  
  Node source;
  
  Node(int x,int y){
    this.passable=true;
    this.x=x;
    this.y=y;
    this.onPath=false;
  }
  
  public void setH(){
    this.h=sqrt(sq(this.x-endPos[0])+sq(this.y-endPos[1]));
  }
}
  public void settings() {  size(1000,1000); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "--present", "--window-color=#666666", "--stop-color=#cccccc", "AStar" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
