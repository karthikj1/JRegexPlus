package karthik.regex.dataStructures;

public class Tree<T> {

    protected TreeNode<T> root;

    public Tree() {
        root = null;
    }

    public Tree(T data, Tree<T> leftSubTree, Tree<T> rightSubTree) {
        root = null;
        addItem(data, leftSubTree, rightSubTree);
    }

    public Tree<T> addItem(T data, Tree<T> leftSubTree, Tree<T> rightSubTree) // always creates new tree with added item at root
    {
        TreeNode<T> node;

        node = new TreeNode<T>(data);
        if (root == null)
            root = node;

        root.setLeft((leftSubTree == null) ? null : leftSubTree.root);
        root.setRight((rightSubTree == null) ? null : rightSubTree.root);

        return this;
    }

    public void traverse() {
        if (root == null)
            return;

        recTraverse(root);
    }

    protected void recTraverse(TreeNode<T> current) {
        if (current == null)
            return;

        recTraverse(current.getLeft());
        current.display();
        recTraverse(current.getRight());

        return;
    }

    public boolean isEmpty() {
        return (root == null);
    }

    public boolean isLeaf(TreeNode<T> current) {
        if ((current.getLeft() == null) && (current.getRight() == null))
            return true;
        else
            return false;
    }

    class TreeNode<T> {

        protected T data;
        protected TreeNode<T> left, right;

        TreeNode(T i) {
            data = i;
            left = right = null;
        }

        T getData() {
            return data;
        }

        void display() {
            System.out.print(data.toString() + " ");
        }

        TreeNode<T> getLeft() {
            return left;
        }

        TreeNode<T> getRight() {
            return right;
        }

        void setLeft(TreeNode<T> newnode) {
            left = newnode;
        }

        void setRight(TreeNode<T> newnode) {
            right = newnode;
        }
    }
}
