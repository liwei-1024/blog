package com.example.service.impl;

import com.example.dao.ArticleMapper;
import com.example.dao.CommentMapper;
import com.example.dao.StatisticMapper;
import com.example.model.domain.Article;
import com.example.model.domain.Statistic;
import com.example.service.IArticleService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.vdurmont.emoji.EmojiParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ArticleServicelmpl implements IArticleService {

    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private StatisticMapper statisticMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private CommentMapper commentMapper;

    @Override
    public PageInfo<Article> selectArticleWithPage(Integer page, Integer count) {
        PageHelper.startPage(page,count);
        List<Article> articleList = articleMapper.selectArticleWithPage();
        for (int i = 0; i < articleList.size(); i++) {
            Article article = articleList.get(i);
            Statistic statistic = statisticMapper.selectStatisticWithArticleId(article.getId());
            article.setHits(statistic.getHits());
            article.setCommentsNum(statistic.getCommentsNum());
        }
        PageInfo<Article> pageInfo = new PageInfo<>(articleList);
        return pageInfo;
    }

    @Override
    public List<Article> getHeatArticles() {
        List<Statistic> list = statisticMapper.getStatistic();
        List<Article> articlelist = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Article article = articleMapper.selectArticleWithId(list.get(i).getArticleId());
            article.setHits(list.get(i).getHits());
            article.setCommentsNum(list.get(i).getCommentsNum());
            articlelist.add(article);
            if(i>=9){
                break;
            }
        }
        return articlelist;
    }



    @Override
    public Article selectArticleWithId(Integer id) {
        //需要引入redis 做缓存处理
        Article article = null;
        //从redis里面拿数据
        Object o = redisTemplate.opsForValue().get("article_" + id);
        //判断这个数据是否存在
        if (o != null){
            article = (Article) o;
        }else {
            //如果没有数据那么需要查询数据库
            article = articleMapper.selectArticleWithId(id);
            //判断这个文章是否有数据
            if (article != null){
                //以key value 形式存储 所有set的时候
                redisTemplate.opsForValue().set("article_"+id,article);
            }
        }
        return article;
    }

    @Override
    public void publish(Article article) {
        //去除表情
        article.setContent(EmojiParser.parseToAliases(article.getContent()));
        article.setCreated(new Date());
        article.setHits(0);
        article.setCommentsNum(0);
        //插入文章，同时插入文章统计数据
        articleMapper.publishArticle(article);
        statisticMapper.addStatistic(article);
    }

    @Override
    public void updateArticleWithId(Article article) {
        article.setModified(new Date());
        //通过文章的id 对应文章信息进行修改
        articleMapper.updateArticleWithId(article);
        //修改文章之后 需要对redis缓存数据进行更新
        redisTemplate.delete("article_" + article.getId());
    }

    //删除文章
    @Override
    public void deleteArticleWithId(int id) {
        // 删除文章的同时，删除对应的缓存
        articleMapper.deleteArticleWithId(id);
        redisTemplate.delete("article_" + id);
        // 同时删除对应文章的统计数据
        statisticMapper.deleteStatisticWithId(id);
        // 同时删除对应文章的评论数据
        commentMapper.deleteCommentWithId(id);
    }
}
