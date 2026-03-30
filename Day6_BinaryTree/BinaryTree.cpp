#include <iostream>
#include <queue>
using namespace std;
struct Node {
    int data;
    Node* left;
    Node* right;

    Node(int value) : data(value), left(nullptr), right(nullptr) {}
};
Node* buildTree(Node* root) {
    int value;
    cout << "Enter node value (or -1 to stop): ";
    cin >> value;

    if (value == -1) {
        return nullptr;
    }

    root = new Node(value);
    cout << "Enter left child of " << value << endl;
    root->left = buildTree(root->left);
    cout << "Enter right child of " << value << endl;
    root->right = buildTree(root->right);

    return root;
}
void inorder(Node* root) {
    if (root == nullptr) {
        return;
    }
    inorder(root->left);
    cout << root->data << " ";
    inorder(root->right);
}
int height(Node* root) {
    if (root == nullptr) {
        return 0;
    }
    int leftHeight = height(root->left);
    int rightHeight = height(root->right);
    return max(leftHeight, rightHeight) + 1;
}
Node* search(Node* root, int key) {
    if (root == nullptr || root->data == key) {
        return root;
    }
    Node* leftSearch = search(root->left, key);
    if (leftSearch != nullptr) {
        return leftSearch;
    }
    return search(root->right, key);
}
void descendants(Node* root){
    int h=height(root);
    if(h==1){
        cout<<"No descendants of node "<<root->data<<endl;
        return;
    }
    queue<Node*> q;
    q.push(root);
    q.push(nullptr); // Marker for end of level
    int level = 0;
    while (!q.empty()) {
        Node* current = q.front();
        q.pop();
        if (current == nullptr) {
            level++;
            if (!q.empty()) {
                q.push(nullptr);
            }
        } else {
            if (level+1 == h) {
                cout << current->data << " ";
            }
            if (current->left) {
                q.push(current->left);
            }
            if (current->right) {
                q.push(current->right);
            }
        }
    }
}
void farthest(Node* root,int target){
    Node* targetNode = search(root, target);
    if (targetNode == nullptr) {
        cout << "Target node not found in the tree." << endl;
        return;
    }
    descendants(targetNode);
}
int main() {
    Node* root = nullptr;
    root = buildTree(root);
    cout << "Inorder Traversal: ";
    inorder(root);
    cout << endl;

    while(true){
        int target;
        cout << "Enter target node value to find its farthest descendants (or -1 to exit): ";
        cin >> target;
        if(target == -1){
            break;
        }
        farthest(root, target);
        cout << endl;
    }

    return 0;
}