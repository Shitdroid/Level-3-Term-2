#include <bits/stdc++.h>
using namespace std;

#define FASTIO ios::sync_with_stdio(0);cin.tie(0)
#define PB push_back
#define MOD (ll)(1e9+7)
#define SZ 100005
#define deb(n) cout<<n<<endl
#define INF INT_MAX

typedef long long ll;
typedef long double ld;
typedef vector<int> vi;
typedef vector<ll> vll;
typedef vector<double> vd;
typedef pair<int, int> pii;
typedef pair<ll, ll> pll;
typedef vector<pii> vp;
typedef vector<pll> vpll;
typedef vector<bool> vb;

int mergeSortInversionCount(vi &arr){
    int inversionCount=0;
    if(arr.size()>1){
        int mid=arr.size()/2;
        vi left(arr.begin(),arr.begin()+mid);
        vi right(arr.begin()+mid,arr.end());
        inversionCount+=mergeSortInversionCount(left);
        inversionCount+=mergeSortInversionCount(right);
        int i=0,j=0,k=0;
        while(i<left.size()&&j<right.size()){
            if(left[i]<=right[j]){
                arr[k]=left[i];
                i++;
            }
            else{
                arr[k]=right[j];
                j++;
                inversionCount+=left.size()-i;
            }
            k++;
        }
        while(i<left.size()){
            arr[k]=left[i];
            i++;
            k++;
        }
        while(j<right.size()){
            arr[k]=right[j];
            j++;
            k++;
        }
    }
    return inversionCount;
}
    


class Puzzle
{
private:
    int size,step;
    vector<vi> table;
    Puzzle* parent;
    pii blankPos;
    int manhattanDistance,hammingDistance;
    string path;
    pii getSuitablePosition(int num){
        num--;
        return make_pair(num/size,num%size);
    }
    int getSingleManhattanDistance(int row, int col){
        if(table[row][col]){
            pii pos=getSuitablePosition(table[row][col]);
            return abs(row-pos.first)+abs(col-pos.second);
        }
        else return 0;
    }
    Puzzle getNewPuzzle(int prevRow, int prevCol, int newRow, int newCol, char move){
        vector<vi> tempTable=table;
        swap(tempTable[prevRow][prevCol],tempTable[newRow][newCol]);
        int num=tempTable[prevRow][prevCol];
        Puzzle tempPuzzle(size,tempTable,make_pair(newRow,newCol),this,step+1,path+move);
        pii pos=getSuitablePosition(num);
        if(pos.first==prevRow&&pos.second==prevCol)tempPuzzle.setHammingDistance(hammingDistance-1);
        if(pos.first!=prevRow||pos.second!=prevCol){
            if(pos.first==newRow && pos.second==newCol)tempPuzzle.setHammingDistance(hammingDistance+1);
        }
        tempPuzzle.setManhattanDistance(manhattanDistance-getSingleManhattanDistance(newRow,newCol)+tempPuzzle.getSingleManhattanDistance(prevRow,prevCol));
        return tempPuzzle;
    }

    int getInversionCount(){
        vi arr;
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                if(table[i][j])arr.push_back(table[i][j]);
            }
        }
        return mergeSortInversionCount(arr);
    }

    void printHelper(Puzzle& puzzle){
        if(puzzle.path==""){
            puzzle.printTable();
            return;
        }
        else{
            vector<vi> tempTable=puzzle.table;
            int i=puzzle.blankPos.first,j=puzzle.blankPos.second;
            char last=puzzle.path[puzzle.path.length()-1];
            if(last=='d')swap(tempTable[i][j],tempTable[i][j-1]);
            else if(last=='u')swap(tempTable[i][j],tempTable[i][j+1]);
            else if(last=='l')swap(tempTable[i][j],tempTable[i+1][j]);
            else if(last=='r')swap(tempTable[i][j],tempTable[i-1][j]);
            Puzzle tempPuzzle(size,tempTable,puzzle.blankPos,puzzle.parent,puzzle.step-1,puzzle.path.substr(0,puzzle.path.length()-1));
        }
    }

public:
    Puzzle(int size,vector<vi> table, pii blankPos, Puzzle* parent,int step=0,string path=""){
        this->size=size;
        this->table=table;
        this->parent=parent;
        this->blankPos=blankPos;
        this->step=step;
        this->path=path;
    }
    Puzzle(const Puzzle &puzzle){
        this->size=puzzle.size;
        this->table=puzzle.table;
        this->parent=puzzle.parent;
        this->blankPos=puzzle.blankPos;
        this->hammingDistance=puzzle.hammingDistance;
        this->manhattanDistance=puzzle.manhattanDistance;
        this->step=puzzle.step;
    }
    void setManhattanDistance(int distance){
        manhattanDistance=distance;
    }
    void setHammingDistance(int distance){
        hammingDistance=distance;
    }
    int getManhattanDistance(){
        return manhattanDistance;
    }
    int getHammingDistance(){
        return hammingDistance;
    }
    int getStep(){
        return step;
    }
    void printTable(){
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                cout<<table[i][j]<<" ";
            }
            cout<<endl;
        }
    }

    vector<Puzzle> getNeighbors(){
        if(parent==this)cout<<"Here also fucked"<<endl;
        int i=blankPos.first,j=blankPos.second;
        vector<Puzzle> neighbours;
        if(i-1>=0)neighbours.push_back(getNewPuzzle(i,j,i-1,j,'l'));
        if(i+1<size)neighbours.push_back(getNewPuzzle(i,j,i+1,j,'r'));
        if(j-1>=0)neighbours.push_back(getNewPuzzle(i,j,i,j-1,'u'));
        if(j+1<size)neighbours.push_back(getNewPuzzle(i,j,i,j+1,'d'));
        return neighbours;
    }

    void printFullPath(){
        cout<<"Printing table: "<<endl;
        printTable();
        if(parent==this){
            cout<<"Everything is fucked"<<endl;
            return;
        };
        if(parent!=nullptr)parent->printFullPath();
    }
    bool checkValidity(){
        int inversionCount=getInversionCount();
        if(size%2){
            if(inversionCount%2==0)return true;
            else return false;
        }
        else{
            if((inversionCount+blankPos.first)%2)return true;
            else return false;
        }
    }
};

class ManhattanCompare
{
public:
    bool operator() (Puzzle& puzzle1, Puzzle& puzzle2){
        if(puzzle1.getManhattanDistance()>puzzle2.getManhattanDistance())return true;
        else return false;
    }
};

class HammingCompare
{
public:
    bool operator() (Puzzle& puzzle1, Puzzle& puzzle2){
        if(puzzle1.getHammingDistance()>puzzle2.getHammingDistance())return true;
        else return false;
    }
};

void getManhattanResult(priority_queue<Puzzle,vector<Puzzle>,ManhattanCompare> queue){
    int expanded=0,explored=0;
    vector<Puzzle> stack;
    while(!queue.empty()){
        Puzzle puzzle=queue.top();
        puzzle.printFullPath();
        queue.pop();
        if(!puzzle.getManhattanDistance()){
            cout<<"Expanded: "<<expanded<<endl;
            cout<<"Explored: "<<explored<<endl;
            cout<<"Optimized Step Numbers: "<<puzzle.getStep()<<endl;
            puzzle.printFullPath();
            return;
        }
        vector<Puzzle> neighbours=puzzle.getNeighbors();
        for(auto neighbour:neighbours){
            explored++;
            queue.push(neighbour);
            cout<<"Explored:"<<explored<<endl;
            neighbour.printFullPath();
        }
        expanded++;
        stack.push_back(puzzle);
    }
}

void getHammingResult(priority_queue<Puzzle,vector<Puzzle>,HammingCompare> queue){
    int expanded=0,explored=0;
    while(!queue.empty()){
        Puzzle puzzle=queue.top();
        queue.pop();
        cout<<"Expanded:"<<expanded<<endl;
        puzzle.printFullPath();
        if(puzzle.getHammingDistance()==0){
            cout<<"Expanded: "<<expanded<<endl;
            cout<<"Explored: "<<explored<<endl;
            cout<<"Optimized Step Numbers: "<<puzzle.getStep()<<endl;
            // puzzle.printFullPath();
            return;
        }
        vector<Puzzle> neighbours=puzzle.getNeighbors();
        for(auto neighbour:neighbours){
            explored++;
            queue.push(neighbour);
        }
        expanded++;
    }
}



int main(){
    FASTIO;
    freopen("in.txt","r",stdin);
    freopen("out.txt","w",stdout);
    int k;
    pii blankPos;
    cin >> k;
    vector<vi> table(k,vi(k));
    vi temp(k*k);
    int cnt=1,hammingDistance=0,manhattanDistance=0;
    for(int i=0;i<k;i++){
        for (int j = 0; j < k; j++)
        {
            cin>>table[i][j];
            temp.push_back(table[i][j]);
            if(table[i][j]==0)blankPos=make_pair(i,j);
            if(table[i][j]!=cnt++ && table[i][j])hammingDistance++;
            if(table[i][j]){
                int num=table[i][j]-1;
                manhattanDistance+=abs(i-num/k)+abs(j-num%k);
            }
        }
        
    }
    Puzzle initState(k,table,blankPos,nullptr);
    if(initState.checkValidity()){
        priority_queue<Puzzle,vector<Puzzle>,ManhattanCompare> manhattanQueue;
        priority_queue<Puzzle,vector<Puzzle>,HammingCompare> hammingQueue;
        initState.setHammingDistance(hammingDistance);
        initState.setManhattanDistance(manhattanDistance);
        manhattanQueue.push(initState);
        hammingQueue.push(initState);
        getManhattanResult(manhattanQueue);
        getHammingResult(hammingQueue);
    }
    else cout<<"Unsolvable Puzzle"<<endl;
    return 0;
}