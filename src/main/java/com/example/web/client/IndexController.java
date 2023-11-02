package com.example.web.client;

import com.example.model.domain.Article;
import com.example.model.domain.Comment;
import com.example.service.IArticleService;
import com.example.service.ICommentService;
import com.example.service.ISiteService;
import com.github.pagehelper.PageInfo;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class IndexController {
    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);


    @Autowired
    private IArticleService articleServiceImpl;
    @Autowired
    private ICommentService commentServiceImpl;
    @Autowired
    private ISiteService siteServiceImpl;

    // 博客首页，会自动跳转到文章页
    @GetMapping(value = "/")
    private String index(HttpServletRequest request) {
        return this.index(request, 1, 5);
    }

    // 文章页
    @GetMapping(value = "/page/{p}")
    public String index(HttpServletRequest request, @PathVariable("p") int page, @RequestParam(value = "count", defaultValue = "5") int count) {
        PageInfo<Article> articles = articleServiceImpl.selectArticleWithPage(page, count);
        // 获取文章热度统计信息
        List<Article> articleList = articleServiceImpl.getHeatArticles();
        request.setAttribute("articles", articles);
        request.setAttribute("articleList", articleList);
        logger.info("分页获取文章信息: 页码 " + page + ",条数 " + count);
        return "client/index";
    }

    //实现文章详情页面查询
    @GetMapping(value = "/article/{id}")
    public String getArticleById(@PathVariable("id") Integer id,
                                 HttpServletRequest request){
        //通过id查询文章信息
        Article article = articleServiceImpl.selectArticleWithId(id);
        if (article != null){
            //查询评论信息
            getArticleComment(request,article);
            //更新文章点击率
            siteServiceImpl.updateStatistics(article);
            //把所有数据放到request域中
            request.setAttribute("article",article);
            return "client/articleDetails";
        }else {
            logger.warn("查询文章详情信息为空，查询文章id："+id);
            return "comm/error_404";
        }
    }

    private void getArticleComment(HttpServletRequest request, Article article) {
        //判断文章是否有评论
        if (article.getAllowComment()){
            //评论分页用的 名称 叫 cp
            String cp = request.getParameter("cp");
            cp = StringUtils.isBlank(cp) ? "1" : cp;
            request.setAttribute("cp", cp);
            PageInfo<Comment> comments = commentServiceImpl.getComments(article.getId(),Integer.parseInt(cp),3);
            request.setAttribute("cp", cp);
            request.setAttribute("comments", comments);
        }
    }
}
