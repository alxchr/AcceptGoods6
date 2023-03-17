package ru.abch.acceptgoods6;

class BarCodesResult {
        public boolean success;
        public int counter;
        public BarCode[] bc;
        BarCodesResult(boolean success, int counter) {
            this.bc = null;
            this.success = success;
            this.counter = counter;
        }
}
