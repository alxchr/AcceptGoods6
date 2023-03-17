package ru.abch.acceptgoods6;

public class GoodsLabel {
	public String goodsId, goodsDescr, goodsArticle, units, cell;
	public int qnt, storeman;
	public GoodsLabel(String goodsId, String goodsDescr, String goodsArticle, String units, int qnt, int storeman, String cell) {
		this.goodsArticle = goodsArticle;
		this.goodsDescr = goodsDescr;
		this.goodsId = goodsId;
		this.qnt = qnt;
		this.storeman = storeman;
		this.units = units;
		this.cell = cell;
	}
}
