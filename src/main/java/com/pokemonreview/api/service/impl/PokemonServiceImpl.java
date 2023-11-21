package com.pokemonreview.api.service.impl;

import com.pokemonreview.api.dto.PageResponse;
import com.pokemonreview.api.dto.PokemonDto;
import com.pokemonreview.api.exceptions.ResourceNotFoundException;
import com.pokemonreview.api.models.Pokemon;
import com.pokemonreview.api.repository.PokemonRepository;
import com.pokemonreview.api.service.PokemonService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor //final로 선언된 변수 생성자 초기화
public class PokemonServiceImpl implements PokemonService {
    private final PokemonRepository pokemonRepository;
    private final ModelMapper modelMapper;

    //생성자 주입
//    public PokemonServiceImpl(PokemonRepository pokemonRepository) {
//        this.pokemonRepository = pokemonRepository;
//    }

    @Override
    public PokemonDto createPokemon(PokemonDto pokemonDto) {
//        Pokemon pokemon = mapToEntity(pokemonDto);
        Pokemon pokemon = modelMapper.map(pokemonDto, Pokemon.class); //modelMapper로 변경

        Pokemon newPokemon = pokemonRepository.save(pokemon);

//        return mapToDto(newPokemon);
        return modelMapper.map(newPokemon, PokemonDto.class); //modelMapper로 변경
    }

    @Override
    public PageResponse<?> getAllPokemon(int pageNo, int pageSize) {
        Pageable pageable =
                PageRequest.of(pageNo, pageSize, Sort.by("id").descending());

        Page<Pokemon> pokemonPage = pokemonRepository.findAll(pageable);
        List<Pokemon> listOfPokemon = pokemonPage.getContent();
        List<PokemonDto> content = listOfPokemon
                .stream() //Stream<Pokemon>
                //entity -> dto
                //.map(entity -> mapToDto(entity)) //Stream<PokemonDto>
                .map(this::mapToDto)
                .collect(Collectors.toList()); //List<PokemonDto>

        PageResponse<PokemonDto> pokemonResponse = new PageResponse<>();
        pokemonResponse.setContent(content);
        pokemonResponse.setPageNo(pokemonPage.getNumber()); //페이지 번호
        pokemonResponse.setPageSize(pokemonPage.getSize()); //페이지 사이즈
        pokemonResponse.setTotalElements(pokemonPage.getTotalElements()); //전체 엘리멘트 개수
        pokemonResponse.setTotalPages(pokemonPage.getTotalPages()); //전체 엘리멘트 개수/페이지 사이즈
        pokemonResponse.setLast(pokemonPage.isLast());

        return pokemonResponse;
    }

    @Override
    public PokemonDto getPokemonById(int id) {
        Pokemon pokemon = getExistPokemon(id);
        return mapToDto(pokemon);
    }

    private Pokemon getExistPokemon(int id) {
        return pokemonRepository
                .findById(id) //Optional<Pokemon>
                .orElseThrow(() ->
                        new ResourceNotFoundException("Pokemon could not be found"));
    }

    @Override
    public PokemonDto updatePokemon(PokemonDto pokemonDto, int id) {
        Pokemon pokemon = getExistPokemon(id);

        //Entity의 setter method 호출을 해도 update query가 실행 됨(Dirty checking)
        if (pokemonDto.getName() != null) {
            pokemon.setName(pokemonDto.getName());
        }
        if (pokemonDto.getType() != null) {
            pokemon.setType(pokemonDto.getType());
        }

        //Pokemon updatedPokemon = pokemonRepository.save(pokemon); -> Transactional이 걸려있기 때문에 할 필요 없음
        return mapToDto(pokemon);
    }

    @Override
    public void deletePokemonId(int id) {
        Pokemon pokemon = getExistPokemon(id);
        pokemonRepository.delete(pokemon);
    }

    //Entity -> Dto
    private PokemonDto mapToDto(Pokemon pokemon) {
        PokemonDto pokemonDto = new PokemonDto();
        pokemonDto.setId(pokemon.getId());
        pokemonDto.setName(pokemon.getName());
        pokemonDto.setType(pokemon.getType());
        return pokemonDto;
    }

    //Dto -> Entity
    private Pokemon mapToEntity(PokemonDto pokemonDto) {
        Pokemon pokemon = new Pokemon();
        pokemon.setName(pokemonDto.getName());
        pokemon.setType(pokemonDto.getType());
        return pokemon;
    }
}