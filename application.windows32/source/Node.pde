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
  
  void setH(){
    this.h=sqrt(sq(this.x-endPos[0])+sq(this.y-endPos[1]));
  }
}
