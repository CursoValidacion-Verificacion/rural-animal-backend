package com.project.demo.rest.publication;

import com.project.demo.logic.entity.direction.TblDirection;
import com.project.demo.logic.entity.direction.TblDirectionRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.photo.TblPhoto;
import com.project.demo.logic.entity.photo.TblPhotoRepository;
import com.project.demo.logic.entity.publication.TblPublication;
import com.project.demo.logic.entity.publication.TblPublicationRepository;
import com.project.demo.logic.entity.user.TblUser;
import com.project.demo.logic.entity.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/publications")
public class PublicationRestController {

    @Autowired
    private TblPublicationRepository tblPublicationRepository;

    @Autowired
    private TblDirectionRepository tblDirectionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TblPhotoRepository tblPhotoRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> getAll(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<TblPublication> publicationsPage = tblPublicationRepository.findAll(pageable);
        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(publicationsPage.getTotalPages());
        meta.setTotalElements(publicationsPage.getTotalElements());
        meta.setPageNumber(publicationsPage.getNumber() + 1);
        meta.setPageSize(publicationsPage.getSize());

        return new GlobalResponseHandler().handleResponse("Publicaciones recuperadas correctamente",
                publicationsPage.getContent(), HttpStatus.OK, meta);
    }

    @GetMapping("/user/{userId}/publications")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SELLER')")
    public ResponseEntity<?> getAllByUserId(@PathVariable long userId,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) String type,
                                            @RequestParam(required = false) String search,
                                            @RequestParam(required = false) String sort,
                                            HttpServletRequest request) {
        Optional<TblUser> foundUser = userRepository.findById(userId);
        if (foundUser.isPresent()) {
            Pageable pageable = PageRequest.of(page - 1, size);
            Page<TblPublication> publicationsPage = tblPublicationRepository.findTblPublicationsByUserId(
                    userId, type, search, sort, pageable);
            Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
            meta.setTotalPages(publicationsPage.getTotalPages());
            meta.setTotalElements(publicationsPage.getTotalElements());
            meta.setPageNumber(publicationsPage.getNumber() + 1);
            meta.setPageSize(publicationsPage.getSize());

            return new GlobalResponseHandler().handleResponse("Publicaciones recuperadas correctamente",
                    publicationsPage.getContent(), HttpStatus.OK, meta);
        } else {
            return new GlobalResponseHandler().handleResponse("Usuario " + userId + " no encontrado",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @GetMapping("/sales")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'BUYER')")
    public ResponseEntity<?> getAllSales(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "6") int size,
                                         HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<TblPublication> salesPage = tblPublicationRepository.findAllSales(pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(salesPage.getTotalPages());
        meta.setTotalElements(salesPage.getTotalElements());
        meta.setPageNumber(salesPage.getNumber() + 1);
        meta.setPageSize(salesPage.getSize());

        return new GlobalResponseHandler().handleResponse(
                "Ventas recuperadas correctamente", salesPage.getContent(), HttpStatus.OK, meta);
    }

    @GetMapping("/auctions")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'BUYER')")
    public ResponseEntity<?> getAllAuctions(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "6") int size,
                                            HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<TblPublication> auctionsPage = tblPublicationRepository.findAllAuctions(pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(auctionsPage.getTotalPages());
        meta.setTotalElements(auctionsPage.getTotalElements());
        meta.setPageNumber(auctionsPage.getNumber() + 1);
        meta.setPageSize(auctionsPage.getSize());

        return new GlobalResponseHandler().handleResponse(
                "Subastas recuperadas correctamente", auctionsPage.getContent(), HttpStatus.OK, meta);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SELLER')")
    public ResponseEntity<?> save(@RequestBody TblPublication tblPublication, HttpServletRequest request) {
        TblDirection tblDirection = new TblDirection();
        tblDirection.setDistrict(tblPublication.getDirection().getDistrict());
        tblDirection.setDistrictId(tblPublication.getDirection().getDistrictId());
        tblDirection.setCanton(tblPublication.getDirection().getCanton());
        tblDirection.setCantonId(tblPublication.getDirection().getCantonId());
        tblDirection.setProvince(tblPublication.getDirection().getProvince());
        tblDirection.setProvinceId(tblPublication.getDirection().getProvinceId());
        tblDirection.setOtherDetails(tblPublication.getDirection().getOtherDetails());

        TblDirection newDirection = tblDirectionRepository.save(tblDirection);
        tblPublication.setDirection(newDirection);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        tblPublication.setUser((TblUser) authentication.getPrincipal());

        for (TblPhoto photo : tblPublication.getPhotos()) {
            photo.setPublication(tblPublication);
        }

        tblPublicationRepository.save(tblPublication);

        return new GlobalResponseHandler().handleResponse("PublicaciÃ³n aÃ±adida con Ã©xito",
                tblPublication, HttpStatus.OK, request);
    }

    @PatchMapping("/{publicationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SELLER')")
    public ResponseEntity<?> patchPublication(@PathVariable Long publicationId,
                                              @RequestBody TblPublication tblPublication,
                                              HttpServletRequest request) {
        Optional<TblPublication> foundPublication = tblPublicationRepository.findById(publicationId);

        if (foundPublication.isPresent()) {
            TblPublication existingPublication = foundPublication.get();

            if (tblPublication.getTitle() != null) {
                existingPublication.setTitle(tblPublication.getTitle());
            }
            if (tblPublication.getSpecie() != null) {
                existingPublication.setSpecie(tblPublication.getSpecie());
            }
            if (tblPublication.getRace() != null) {
                existingPublication.setRace(tblPublication.getRace());
            }
            if (tblPublication.getGender() != null) {
                existingPublication.setGender(tblPublication.getGender());
            }
            if (tblPublication.getWeight() != null) {
                existingPublication.setWeight(tblPublication.getWeight());
            }
            if (tblPublication.getBirthDate() != null) {
                existingPublication.setBirthDate(tblPublication.getBirthDate());
            }
            if (tblPublication.getSenasaCertificate() != null) {
                existingPublication.setSenasaCertificate(tblPublication.getSenasaCertificate());
            }
            if (tblPublication.getPrice() != null) {
                existingPublication.setPrice(tblPublication.getPrice());
            }
            if (tblPublication.getStartDate() != null) {
                existingPublication.setStartDate(tblPublication.getStartDate());
            }
            if (tblPublication.getEndDate() != null) {
                existingPublication.setEndDate(tblPublication.getEndDate());
            }
            if (tblPublication.getMinimumIncrease() != null) {
                existingPublication.setMinimumIncrease(tblPublication.getMinimumIncrease());
            }
            if (tblPublication.getType() != null) {
                existingPublication.setType(tblPublication.getType());
            }
            if (tblPublication.getState() != null) {
                existingPublication.setState(tblPublication.getState());
            }
            if (tblPublication.getCreationDate() != null) {
                existingPublication.setCreationDate(tblPublication.getCreationDate());
            }
            if (tblPublication.getDirection() != null) {
                TblDirection currentDirection = existingPublication.getDirection();
                if (currentDirection == null) {
                    TblDirection newDirection = new TblDirection();
                    newDirection.setDistrict(tblPublication.getDirection().getDistrict());
                    newDirection.setDistrictId(tblPublication.getDirection().getDistrictId());
                    newDirection.setCanton(tblPublication.getDirection().getCanton());
                    newDirection.setCantonId(tblPublication.getDirection().getCantonId());
                    newDirection.setProvince(tblPublication.getDirection().getProvince());
                    newDirection.setProvinceId(tblPublication.getDirection().getProvinceId());
                    newDirection.setOtherDetails(tblPublication.getDirection().getOtherDetails());
                    existingPublication.setDirection(tblDirectionRepository.save(newDirection));
                } else {
                    if (tblPublication.getDirection().getDistrict() != null) {
                        currentDirection.setDistrict(tblPublication.getDirection().getDistrict());
                    }
                    if (tblPublication.getDirection().getDistrictId() != null) {
                        currentDirection.setDistrictId(tblPublication.getDirection().getDistrictId());
                    }
                    if (tblPublication.getDirection().getCanton() != null) {
                        currentDirection.setCanton(tblPublication.getDirection().getCanton());
                    }
                    if (tblPublication.getDirection().getCantonId() != null) {
                        currentDirection.setCantonId(tblPublication.getDirection().getCantonId());
                    }
                    if (tblPublication.getDirection().getProvince() != null) {
                        currentDirection.setProvince(tblPublication.getDirection().getProvince());
                    }
                    if (tblPublication.getDirection().getProvinceId() != null) {
                        currentDirection.setProvinceId(tblPublication.getDirection().getProvinceId());
                    }
                    if (tblPublication.getDirection().getOtherDetails() != null) {
                        currentDirection.setOtherDetails(tblPublication.getDirection().getOtherDetails());
                    }
                    tblDirectionRepository.save(currentDirection);
                }
            }
            if (tblPublication.getPhotos() != null) {
                existingPublication.setPhotos(tblPublication.getPhotos());
            }

            TblPublication savedPublication = tblPublicationRepository.save(existingPublication);

            return new GlobalResponseHandler().handleResponse("PublicaciÃ³n actualizda con Ã©xito",
                    savedPublication, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("PublicaciÃ³n " + publicationId + " no encontrada",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @GetMapping("/filtered")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SELLER', 'BUYER')")
    public ResponseEntity<?> getFilteredPublications(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     @RequestParam(required = false) String type,
                                                     @RequestParam(required = false) String search,
                                                     @RequestParam(required = false) String sort,
                                                     HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<TblPublication> filteredPublications = tblPublicationRepository.findFilteredPublications(
                type, search, sort, pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(filteredPublications.getTotalPages());
        meta.setTotalElements(filteredPublications.getTotalElements());
        meta.setPageNumber(filteredPublications.getNumber() + 1);
        meta.setPageSize(filteredPublications.getSize());

        return new GlobalResponseHandler().handleResponse("Publicaciones filtradas correctamente",
                filteredPublications.getContent(), HttpStatus.OK, meta);
    }
}
