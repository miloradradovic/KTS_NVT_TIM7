package com.project.tim7.api;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.project.tim7.dto.CategoryDTO;
import com.project.tim7.helper.CategoryMapper;
import com.project.tim7.model.Category;
import com.project.tim7.model.Person;
import com.project.tim7.service.CategoryService;

@CrossOrigin(origins = "https://localhost:4200")
@RestController
@RequestMapping(value="/categories", produces = MediaType.APPLICATION_JSON_VALUE)
public class CategoryController  {
	
	@Autowired
	CategoryService categoryService;
	
	private CategoryMapper catMapper;
	
	public CategoryController() {
		super();
		this.catMapper = new CategoryMapper();
	}

	@PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<List<CategoryDTO>> getAllCategories(){
		
		List<Category> categories = categoryService.findAll();
		
        return new ResponseEntity<>(toCategoryDTOList(categories), HttpStatus.OK);

	}
	
	@PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
	@RequestMapping(value= "/by-page",method = RequestMethod.GET)
    public ResponseEntity<Page<CategoryDTO>> getAllCategories(Pageable pageable) {
        Page<Category> page = categoryService.findAll(pageable);
        List<CategoryDTO> dtos = toCategoryDTOList(page.toList());
        Page<CategoryDTO> pageCategoryDTOS = new PageImpl<>(dtos,page.getPageable(),page.getTotalElements());

        return new ResponseEntity<>(pageCategoryDTOS, HttpStatus.OK);
    }
	
	@PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
	@RequestMapping(method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> updateCategory(@Valid @RequestBody CategoryDTO categoryDTO){
		
		Category newCategory = categoryService.update(catMapper.toEntity(categoryDTO));
		if(newCategory != null) {
			categoryDTO.setId(newCategory.getId());
			return new ResponseEntity<>(categoryDTO, HttpStatus.OK);
		}else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
	}
	
	@PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
	@RequestMapping(method= RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createCategory(@Valid @RequestBody CategoryDTO category){
		
		Category newCategory = categoryService.saveOne(catMapper.toEntity(category));
		if(newCategory != null) {
			category.setId(newCategory.getId());
			return new ResponseEntity<>(category, HttpStatus.CREATED);
		}else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
    }
	
	@PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
	@RequestMapping(value="/{id}", method=RequestMethod.DELETE)
    public ResponseEntity<String> deleteCategory(@PathVariable Integer id){

        if(categoryService.delete(id)) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>("Deleting failed.", HttpStatus.BAD_REQUEST);
    }

    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
	@RequestMapping(value="/by-id/{id}", method=RequestMethod.GET)
	public ResponseEntity<CategoryDTO> getCategory(@PathVariable Integer id){
		Category found = categoryService.findOne(id);
		if(found == null){
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}else{
			return new ResponseEntity<CategoryDTO>(catMapper.toDto(found), HttpStatus.OK);
		}
	}
    
    @PreAuthorize("hasRole('ROLE_REGISTERED')")
	@RequestMapping(value= "/subscribed/{id}",method = RequestMethod.GET)
    public ResponseEntity<List<CategoryDTO>> getSubscribedCategories(@PathVariable Integer id) {
        List<Category> list = categoryService.findSubscribedCategories(id);
        List<CategoryDTO> dtos = toCategoryDTOList(list);

        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

	private List<CategoryDTO> toCategoryDTOList(List<Category> categories) {
		ArrayList<CategoryDTO> dtos = new ArrayList<CategoryDTO>();
		for(Category category : categories) {
			CategoryDTO dto = catMapper.toDto(category);
			dtos.add(dto);
		}
		return dtos;
	}

}
