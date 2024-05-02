package com.icehufs.icebreaker.service.implement;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.icehufs.icebreaker.dto.request.article.PostArticleRequestDto;
import com.icehufs.icebreaker.dto.response.ResponseDto;
import com.icehufs.icebreaker.dto.response.article.GetArticleListResponseDto;
import com.icehufs.icebreaker.dto.response.article.GetArticleResponseDto;
import com.icehufs.icebreaker.dto.response.article.PostArticleResponseDto;
import com.icehufs.icebreaker.dto.response.article.PutFavoriteResponseDto;
import com.icehufs.icebreaker.entity.Article;
import com.icehufs.icebreaker.entity.FavoriteEntity;
import com.icehufs.icebreaker.repository.ArticleRepository;
import com.icehufs.icebreaker.repository.ArtileListViewRepository;
import com.icehufs.icebreaker.repository.FavoriteRepository;
import com.icehufs.icebreaker.repository.UserRepository;
import com.icehufs.icebreaker.service.ArticleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArticleServiceImplement implements ArticleService {

    private final ArtileListViewRepository artileListViewRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    
    @Override
    public ResponseEntity<? super PostArticleResponseDto> postArticle(PostArticleRequestDto dto, String email){
        try{
            boolean existedEmail = userRepository.existsByEmail(email);
            if (!existedEmail) return PostArticleResponseDto.notExistUser();

            Article articleEntity = new Article(dto, email);
            articleRepository.save(articleEntity);
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }
        return PostArticleResponseDto.success();
    }
    

    @Override
    public ResponseEntity<? super GetArticleResponseDto> getArticle(Integer articleNum) {
        Article articleEntity = null;

        try {
            articleEntity = articleRepository.findByArticleNum(articleNum);
            if (articleEntity == null) return GetArticleResponseDto.noExistArticle();
            articleEntity.IncreaseViewCount();
            articleRepository.save(articleEntity);
        } catch (Exception exception){
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }
        return GetArticleResponseDto.success(articleEntity);
    }


    @Override
    public ResponseEntity<? super GetArticleListResponseDto> getArticleList() {
        List<Article> articleListViewEntities = new ArrayList<>();
        try{
            articleListViewEntities = artileListViewRepository.findAll();
        }catch(Exception exception) {
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }
        return GetArticleListResponseDto.success(articleListViewEntities);
    }


    @Override
    public ResponseEntity<? super PutFavoriteResponseDto> putFavorite(Integer articleNum, String email) {

        Article articleEntity = null;
        try {
            boolean existedUser = userRepository.existsByEmail(email);
            if (!existedUser) return PutFavoriteResponseDto.notExistUser();

            articleEntity = articleRepository.findByArticleNum(articleNum);
            if (articleEntity == null) return PutFavoriteResponseDto.noExistArticle();

            FavoriteEntity favoriteEntity = favoriteRepository.findByArticleNumAndUserEmail(articleNum, email);
            if (favoriteEntity == null){
                favoriteEntity = new FavoriteEntity(email, articleNum);
                favoriteRepository.save(favoriteEntity);
                articleEntity.IncreaseFavoriteCount();
            }
            else{
                favoriteRepository.delete(favoriteEntity);
                articleEntity.decreaseFavoriteCount();
            }
            articleRepository.save(articleEntity);
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }
        return PutFavoriteResponseDto.success();
    }
}
