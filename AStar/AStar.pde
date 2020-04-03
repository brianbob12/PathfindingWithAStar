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

void setup(){
  size(1000,1000);
  nodes=new Node[int(width/cellSize)][int(height/cellSize)];
  for(int i=0;i<int(width/cellSize);i++){
    for(int j=0;j<int(height/cellSize);j++){
      nodes[i][j]=new Node(i,j);
    }
  }
  int obsticlesMade=0;
  int tempX;
  int tempY;
  while (obsticlesMade<startingObsticles){
    tempX=int(random(width/cellSize));
    tempY=int(random(width/cellSize));
    if(nodes[tempX][tempY].passable){
      nodes[tempX][tempY].passable=false;
      obsticlesMade+=1;
    }
  }
}

void draw(){
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
  
  if(mousePressed&&mouseX<=int(width/cellSize)*cellSize&&mouseY<=int(height/cellSize)*cellSize&&mouseX>0&&mouseY>0){
    if (key == CODED&&keyCode == SHIFT&&!started&&keyPressed&&(done||!started)){
      if(nodes[int(mouseX/cellSize)][int(mouseY/cellSize)].passable){
        if(mouseButton == LEFT){
          startPos[0]=int(mouseX/cellSize);
          startPos[1]=int(mouseY/cellSize);
        }
        else if(mouseButton == RIGHT){
          endPos[0]=int(mouseX/cellSize);
          endPos[1]=int(mouseY/cellSize);
        }
      }
    }
    else{
      if(mouseButton == LEFT){
        nodes[int(mouseX/cellSize)][int(mouseY/cellSize)].passable=false;
      }
      else if(mouseButton == RIGHT){
        nodes[int(mouseX/cellSize)][int(mouseY/cellSize)].passable=true;
      }
    }
  }  
}

void keyPressed(){
  if(key==' '&&(done||!started)){
    if(startPos[0]==-1||endPos[0]==-1){return;}
    if(startPos[0]==endPos[0]&&startPos[1]==endPos[1]){return;}
    startPathfinding();
  }
  else if(key=='s'){
    show=!show;
  }
}

void startPathfinding(){
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
void step(){
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
    temp[i+(-1*int(moved))]=open[i];
  }
  open=temp;
}

void explore(int x,int y){//explores node at x y
  
  for(int i=x-1;i<x+2;i+=2){
    if(i<0||i>=int(width/cellSize)){continue;}
    if(!nodes[i][y].closed&&nodes[i][y].passable){
      reveal(nodes[i][y],nodes[x][y]);
    }
  }
  for(int i=y-1;i<y+2;i+=2){
    if(i<0||i>=int(height/cellSize)){continue;}
    if(!nodes[x][i].closed&&nodes[x][i].passable){
      reveal(nodes[x][i],nodes[x][y]);
    }
  }
}
void reveal(Node subject,Node source){//reveal the subject
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

void finishUp(){
  pathLength=0;
  Node temp=nodes[endPos[0]][endPos[1]].source;
  while(!(temp.x==startPos[0]&&temp.y==startPos[1])){
    pathLength+=1;
    temp.onPath=true;
    temp=temp.source;
  }
}
